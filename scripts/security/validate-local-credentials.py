#!/usr/bin/env python3
"""RN-SEC106-003: explicit, local-only credential diagnostics; never a release gate."""
import argparse
import http.client
import json
import os
from pathlib import Path
import re
import socket
import subprocess
import sys
from urllib.parse import urlencode

TIMEOUT = 5
LIMIT = 65536
SERVICES = {'spring', 'next', 'strapi', 'postgres', 'redis', 'signing-key'}


class InvalidConfig(Exception):
    pass


def git(repo, *args):
    result = subprocess.run(['git', '-C', str(repo), *args], capture_output=True, timeout=TIMEOUT)
    if result.returncode:
        raise InvalidConfig()
    return result.stdout.decode('utf-8')


def private_file(repo, name):
    path = (repo / name).resolve()
    try:
        relative = path.relative_to(repo)
    except ValueError:
        raise InvalidConfig() from None
    # Untracked AND ignored: tracked files remain forbidden even with ignore rules.
    if git(repo, 'ls-files', '--', str(relative)).strip():
        raise InvalidConfig()
    git(repo, 'check-ignore', '--', str(relative))
    if not path.is_file() or path.stat().st_size > LIMIT:
        raise InvalidConfig()
    return path


def source_value(repo, source):
    if not isinstance(source, dict) or not re.fullmatch(r'[A-Za-z_][A-Za-z0-9_]*', source.get('key', '')):
        raise InvalidConfig()
    if source.get('kind') == 'env':
        content = private_file(repo, source['path']).read_text()
    elif source.get('kind') == 'git':
        sha = source.get('commit', '')
        path = source.get('path', '')
        if not re.fullmatch(r'[a-f0-9]{40}', sha) or not path or path.startswith('/') or '..' in Path(path).parts:
            raise InvalidConfig()
        # Explicit immutable commit and file; never search all history for credentials.
        if int(git(repo, 'cat-file', '-s', f'{sha}:{path}')) > LIMIT:
            raise InvalidConfig()
        content = git(repo, 'show', f'{sha}:{path}')
    else:
        raise InvalidConfig()
    values = []
    for line in content.splitlines():
        match = re.fullmatch(r'\s*(?:export\s+)?' + re.escape(source['key']) + r'\s*[=:]\s*(.*?)\s*', line)
        if match:
            value = match[1]
            if len(value) >= 2 and value[0] == value[-1] and value[0] in '\"\'':
                value = value[1:-1]
            if not value or '\x00' in value or '${' in value or '$(' in value:
                raise InvalidConfig()
            values.append(value)
    if len(values) != 1:
        raise InvalidConfig()
    return values[0]


def validate(items):
    if not isinstance(items, list) or not 1 <= len(items) <= 20:
        raise InvalidConfig()
    seen = set()
    for item in items:
        if not isinstance(item, dict):
            raise InvalidConfig()
        ident = item.get('id', '')
        if not isinstance(ident, str) or not re.fullmatch(r'item-[0-9]{3}', ident) or ident in seen:
            raise InvalidConfig()
        seen.add(ident)
        if item.get('service') not in SERVICES or item.get('environment') != 'local-development':
            raise InvalidConfig()
        if item['service'] == 'signing-key':
            continue
        if item.get('host') not in ('127.0.0.1', '::1') or type(item.get('port')) is not int or not 1 <= item['port'] <= 65535:
            raise InvalidConfig()
        for field in ('username', 'database', 'companySlug'):
            value = item.get(field, '')
            if not isinstance(value, str) or len(value) > 256 or any(ord(c) < 32 for c in value):
                raise InvalidConfig()
        if item['service'] in {'spring', 'next', 'strapi', 'postgres'} and not item.get('username'):
            raise InvalidConfig()
        if item['service'] == 'postgres' and not re.fullmatch(r'[A-Za-z0-9_-]+', item.get('database', '')):
            raise InvalidConfig()
        if not isinstance(item.get('source'), dict):
            raise InvalidConfig()


class LocalHTTP:
    """Direct numeric loopback connections: no proxy, DNS, redirect, or raw logging."""
    def __init__(self, item):
        self.item = item
        self.cookies = {}

    def request(self, method, path, data=None, form=False):
        conn = http.client.HTTPConnection(self.item['host'], self.item['port'], timeout=TIMEOUT)
        headers = {'Accept': 'application/json'}
        host = self.item['host']
        headers['Origin'] = f'http://[{host}]:{self.item["port"]}' if ':' in host else f'http://{host}:{self.item["port"]}'
        if self.cookies:
            headers['Cookie'] = '; '.join(f'{k}={v}' for k, v in self.cookies.items())
        body = None
        if data is not None:
            body = urlencode(data) if form else json.dumps(data)
            headers['Content-Type'] = 'application/x-www-form-urlencoded' if form else 'application/json'
        try:
            conn.request(method, path, body, headers)
            response = conn.getresponse()
            raw = response.read(LIMIT + 1)
            if len(raw) > LIMIT:
                raise ValueError()
            from http.cookies import SimpleCookie
            for key, value in response.getheaders():
                if key.lower() == 'set-cookie':
                    parsed = SimpleCookie()
                    parsed.load(value)
                    self.cookies.update({k: v.value for k, v in parsed.items()})
            try:
                payload = json.loads(raw)
            except (ValueError, UnicodeError):
                payload = None
            return response.status, payload
        finally:
            conn.close()


def http_probe(item, secret):
    client = LocalHTTP(item)
    data = {'email': item['username'], 'password': secret}
    if item.get('companySlug'):
        data['companySlug'] = item['companySlug']
    service = item['service']
    if service != 'next':
        path = '/api/v1/auth/authenticate' if service == 'spring' else '/admin/login'
        status, payload = client.request('POST', path, data)
        if status in (401, 403):
            return 'ACESSO_NEGADO', 'AUTH_REJECTED'
        if status != 200 or not isinstance(payload, dict):
            return 'INCONCLUSIVO', 'UNEXPECTED_RESPONSE'
        token = payload.get('token') if service == 'spring' else (payload.get('data') or {}).get('token')
        return ('ACESSO_CONFIRMADO', 'AUTH_ACCEPTED') if isinstance(token, str) and token else ('INCONCLUSIVO', 'MISSING_AUTH_PROOF')
    status, payload = client.request('GET', '/api/auth/csrf')
    if status != 200 or not isinstance(payload, dict) or not isinstance(payload.get('csrfToken'), str):
        return 'INCONCLUSIVO', 'CSRF_UNAVAILABLE'
    data.update(csrfToken=payload['csrfToken'], json='true')
    attempted = False
    try:
        attempted = True
        status, payload = client.request('POST', '/api/auth/callback/credentials', data, form=True)
        if status in (401, 403):
            outcome = ('ACESSO_NEGADO', 'AUTH_REJECTED')
        elif status != 200 or not isinstance(payload, dict) or 'error=' in str(payload.get('url', '')):
            outcome = ('INCONCLUSIVO', 'UNEXPECTED_RESPONSE')
        else:
            status, session = client.request('GET', '/api/auth/session')
            user = session.get('user') if isinstance(session, dict) else None
            outcome = ('ACESSO_CONFIRMADO', 'AUTH_ACCEPTED') if status == 200 and isinstance(user, dict) and user.get('email') == item['username'] else ('INCONCLUSIVO', 'MISSING_AUTH_PROOF')
    finally:
        if attempted:
            try:
                status, csrf = client.request('GET', '/api/auth/csrf')
                if status != 200 or not isinstance(csrf, dict) or not csrf.get('csrfToken'):
                    return 'INCONCLUSIVO', 'SESSION_CLEANUP_FAILED'
                status, _ = client.request('POST', '/api/auth/signout', {'csrfToken': csrf['csrfToken'], 'json': 'true'}, form=True)
                if status != 200:
                    return 'INCONCLUSIVO', 'SESSION_CLEANUP_FAILED'
            except (OSError, ValueError, http.client.HTTPException):
                return 'INCONCLUSIVO', 'SESSION_CLEANUP_FAILED'
    return outcome


def redis_probe(item, secret):
    def send(stream, *parts):
        encoded = [p.encode() for p in parts]
        stream.write(b'*%d\r\n' % len(parts) + b''.join(b'$%d\r\n' % len(p) + p + b'\r\n' for p in encoded))
        stream.flush()
        return stream.readline(4096)
    with socket.create_connection((item['host'], item['port']), timeout=TIMEOUT) as conn:
        with conn.makefile('rwb') as stream:
            auth = ('AUTH', item['username'], secret) if item.get('username') else ('AUTH', secret)
            response = send(stream, *auth)
            if response.startswith((b'-WRONGPASS', b'-NOAUTH')):
                return 'ACESSO_NEGADO', 'AUTH_REJECTED'
            if response != b'+OK\r\n':
                return 'INCONCLUSIVO', 'UNEXPECTED_RESPONSE'
            pong = send(stream, 'PING')
            send(stream, 'QUIT')
            return ('ACESSO_CONFIRMADO', 'AUTH_ACCEPTED') if pong == b'+PONG\r\n' else ('INCONCLUSIVO', 'MISSING_AUTH_PROOF')


def postgres_probe(item, secret):
    # Minimal environment prevents inherited service/host/SSL options or .psqlrc execution.
    env = {'PATH': os.defpath, 'PGPASSWORD': secret, 'PGCONNECT_TIMEOUT': str(TIMEOUT),
           'PGOPTIONS': '-c default_transaction_read_only=on -c statement_timeout=5000', 'LC_ALL': 'C'}
    result = subprocess.run(['psql', '-X', '-w', '-A', '-t', '-h', item['host'], '-p', str(item['port']),
                             '-U', item['username'], '-d', item['database'], '-c', 'SELECT 1'],
                            env=env, capture_output=True, timeout=TIMEOUT + 1)
    if result.returncode == 0 and result.stdout.strip() == b'1':
        return 'ACESSO_CONFIRMADO', 'AUTH_ACCEPTED'
    if b'password authentication failed' in result.stderr:
        return 'ACESSO_NEGADO', 'AUTH_REJECTED'
    return 'INCONCLUSIVO', 'DATABASE_UNAVAILABLE'


def diagnose(item, secret):
    try:
        if item['service'] == 'signing-key':
            return 'NAO_VERIFICAVEL', 'SIGNING_KEY_NOT_LOGIN'
        if item['service'] == 'redis':
            return redis_probe(item, secret)
        if item['service'] == 'postgres':
            return postgres_probe(item, secret)
        return http_probe(item, secret)
    except (OSError, ValueError, TypeError, AttributeError, http.client.HTTPException, subprocess.SubprocessError):
        return 'INCONCLUSIVO', 'PROBE_UNAVAILABLE'


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, required=True)
    parser.add_argument('--mapping', required=True)
    parser.add_argument('--execute', action='store_true')
    args = parser.parse_args(argv)
    try:
        repo = args.repo.resolve()
        items = json.loads(private_file(repo, args.mapping).read_text())
        validate(items)
        # Resolve all sources before making any network request.
        secrets = [None if i['service'] == 'signing-key' else source_value(repo, i['source']) for i in items]
        results = []
        for item, secret in zip(items, secrets):
            state, reason = diagnose(item, secret) if args.execute else ('NAO_EXECUTADO', 'CONFIG_VALIDATED')
            results.append({'id': item['id'], 'service': item['service'], 'state': state, 'reason': reason})
        print(json.dumps({'results': results, 'releases_findings': False}, ensure_ascii=True))
        return 0 if not args.execute or all(r['state'] == 'ACESSO_NEGADO' for r in results) else 3
    except (InvalidConfig, OSError, ValueError, TypeError, KeyError, subprocess.SubprocessError):
        print(json.dumps({'error': 'INVALID_PRIVATE_MAPPING', 'releases_findings': False}))
        return 2


if __name__ == '__main__':
    sys.exit(main())
