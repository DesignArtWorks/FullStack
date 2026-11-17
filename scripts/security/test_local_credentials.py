"""Synthetic-only contract tests for RN-SEC106-003 / #106."""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import socketserver
import subprocess
import tempfile
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('diagnostic', Path(__file__).with_name('validate-local-credentials.py'))
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_GET(self):
        self.respond()

    def do_POST(self):
        self.rfile.read(int(self.headers.get('Content-Length', 0)))
        self.respond()

    def respond(self):
        self.server.paths.append(self.path)
        status, body = self.server.replies.get(self.path, (404, {}))
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Set-Cookie', 'session=synthetic; Path=/')
        if status == 302:
            self.send_header('Location', 'https://example.invalid/do-not-follow')
        self.end_headers()
        self.wfile.write(json.dumps(body).encode())


class Tests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = Path(self.temp.name)
        self.git('init', '-q')
        self.git('config', 'user.email', 'fixture@example.test')
        self.git('config', 'user.name', 'Fixture')
        (self.repo / '.gitignore').write_text('.private/\n')
        (self.repo / '.private').mkdir()
        (self.repo / '.private/secrets.env').write_text('PASSWORD="synthetic-only-password"\n')
        self.item = {'id': 'item-001', 'service': 'spring', 'environment': 'local-development',
                     'host': '127.0.0.1', 'port': 12345, 'username': 'fixture@example.test',
                     'source': {'kind': 'env', 'path': '.private/secrets.env', 'key': 'PASSWORD'}}

    def git(self, *args):
        return subprocess.check_output(['git', '-C', str(self.repo), *args], stderr=subprocess.DEVNULL).decode().strip()

    def server(self, replies):
        server = ThreadingHTTPServer(('127.0.0.1', 0), Handler)
        server.replies, server.paths = replies, []
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.server_close)
        self.addCleanup(server.shutdown)
        self.item['port'] = server.server_port
        return server

    def cli(self, execute=True):
        (self.repo / '.private/mapping.json').write_text(json.dumps([self.item]))
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            code = m.main(['--repo', str(self.repo), '--mapping', '.private/mapping.json'] + (['--execute'] if execute else []))
        self.assertNotIn('synthetic-only-password', output.getvalue())
        self.assertNotIn('fixture@example.test', output.getvalue())
        return code, json.loads(output.getvalue())

    def test_dry_run_never_connects(self):
        with patch.object(m.socket, 'create_connection', side_effect=AssertionError):
            code, result = self.cli(False)
        self.assertEqual(0, code)
        self.assertFalse(result['releases_findings'])

    def test_external_host_and_invalid_ids_rejected_before_source_or_network(self):
        for host in ('localhost', 'example.com', '127.0.0.1.evil', '169.254.169.254', '0.0.0.0'):
            self.item['host'] = host
            self.assertEqual(2, self.cli()[0])
        self.item['host'] = '127.0.0.1'
        self.item['id'] = 'secret-as-id'
        self.assertEqual(2, self.cli()[0])

    def test_all_sources_resolved_before_authentication(self):
        self.item['source']['key'] = 'MISSING'
        with patch.object(m, 'diagnose', side_effect=AssertionError):
            self.assertEqual(2, self.cli()[0])

    def test_spring_success_requires_token_not_just_200(self):
        server = self.server({'/api/v1/auth/authenticate': (200, {'token': 'synthetic-token'})})
        code, result = self.cli()
        self.assertEqual(3, code)  # Access is a remediation signal, not a clean gate.
        self.assertEqual('ACESSO_CONFIRMADO', result['results'][0]['state'])
        self.assertEqual(['/api/v1/auth/authenticate'], server.paths)
        server.replies['/api/v1/auth/authenticate'] = (200, {})
        self.assertEqual('INCONCLUSIVO', self.cli()[1]['results'][0]['state'])

    def test_denied_and_unavailable_are_distinct(self):
        server = self.server({'/api/v1/auth/authenticate': (401, {})})
        self.assertEqual('ACESSO_NEGADO', self.cli()[1]['results'][0]['state'])
        server.replies['/api/v1/auth/authenticate'] = (503, {})
        self.assertEqual('INCONCLUSIVO', self.cli()[1]['results'][0]['state'])

    def test_redirect_never_followed_and_proxy_ignored(self):
        server = self.server({'/api/v1/auth/authenticate': (302, {})})
        with patch.dict(m.os.environ, {'HTTP_PROXY': 'http://example.invalid:80'}):
            self.assertEqual('INCONCLUSIVO', self.cli()[1]['results'][0]['state'])
        self.assertEqual(['/api/v1/auth/authenticate'], server.paths)

    def test_next_proves_session_and_logs_out(self):
        self.item['service'] = 'next'
        server = self.server({'/api/auth/csrf': (200, {'csrfToken': 'synthetic-csrf'}),
                              '/api/auth/callback/credentials': (200, {'url': '/dashboard'}),
                              '/api/auth/session': (200, {'user': {'email': self.item['username']}}),
                              '/api/auth/signout': (200, {'url': '/login'})})
        self.assertEqual('ACESSO_CONFIRMADO', self.cli()[1]['results'][0]['state'])
        self.assertEqual('/api/auth/signout', server.paths[-1])
        server.replies['/api/auth/signout'] = (500, {})
        self.assertEqual('SESSION_CLEANUP_FAILED', self.cli()[1]['results'][0]['reason'])

    def test_strapi_admin_only(self):
        self.item['service'] = 'strapi'
        server = self.server({'/admin/login': (200, {'data': {'token': 'synthetic-token'}})})
        self.assertEqual('ACESSO_CONFIRMADO', self.cli()[1]['results'][0]['state'])
        self.assertEqual(['/admin/login'], server.paths)

    def test_signing_key_never_read_or_sent(self):
        self.item['service'] = 'signing-key'
        self.item.pop('source')
        with patch.object(m, 'source_value', side_effect=AssertionError):
            self.assertEqual('NAO_VERIFICAVEL', self.cli()[1]['results'][0]['state'])

    def test_untracked_unignored_tracked_and_symlink_escape_rejected(self):
        (self.repo / 'plain.env').write_text('PASSWORD=synthetic\n')
        self.item['source']['path'] = 'plain.env'
        self.assertEqual(2, self.cli()[0])
        self.git('add', '-f', '.private/secrets.env')
        self.item['source']['path'] = '.private/secrets.env'
        self.assertEqual(2, self.cli()[0])
        (self.repo / '.private/link.env').symlink_to('/etc/passwd')
        self.item['source']['path'] = '.private/link.env'
        self.assertEqual(2, self.cli()[0])

    def test_history_exact_commit_and_key(self):
        (self.repo / 'historical.env').write_text('PASSWORD="synthetic-history"\n')
        self.git('add', 'historical.env')
        self.git('commit', '-qm', 'Synthetic only')
        source = {'kind': 'git', 'commit': self.git('rev-parse', 'HEAD'), 'path': 'historical.env', 'key': 'PASSWORD'}
        self.assertEqual('synthetic-history', m.source_value(self.repo, source))
        source['commit'] = 'HEAD'
        with self.assertRaises(m.InvalidConfig):
            m.source_value(self.repo, source)

    def test_duplicate_or_interpolated_values_rejected(self):
        for content in ('PASSWORD=a\nPASSWORD=b\n', 'PASSWORD=${EXTERNAL}\n', 'PASSWORD=$(command)\n'):
            (self.repo / '.private/secrets.env').write_text(content)
            self.assertEqual(2, self.cli()[0])

    def test_postgres_no_secret_in_arguments_and_readonly(self):
        self.item.update(service='postgres', database='fixture')
        with patch.object(m.subprocess, 'run', return_value=subprocess.CompletedProcess([], 0, b'1\n', b'')) as run:
            self.assertEqual(('ACESSO_CONFIRMADO', 'AUTH_ACCEPTED'), m.postgres_probe(self.item, 'synthetic-password'))
        args, kwargs = run.call_args
        self.assertNotIn('synthetic-password', repr(args))
        self.assertIn('default_transaction_read_only=on', kwargs['env']['PGOPTIONS'])
        self.assertEqual('synthetic-password', kwargs['env']['PGPASSWORD'])
        self.assertEqual('SELECT 1', args[0][-1])

    def test_redis_auth_ping_quit(self):
        commands = []
        class Redis(socketserver.StreamRequestHandler):
            def handle(inner):
                while True:
                    line = inner.rfile.readline()
                    if not line:
                        return
                    parts = []
                    for _ in range(int(line[1:])):
                        size = int(inner.rfile.readline()[1:])
                        parts.append(inner.rfile.read(size))
                        inner.rfile.read(2)
                    commands.append(parts[0])
                    inner.wfile.write(b'+PONG\r\n' if parts[0] == b'PING' else b'+OK\r\n')
                    if parts[0] == b'QUIT':
                        return
        server = socketserver.TCPServer(('127.0.0.1', 0), Redis)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        self.addCleanup(server.server_close)
        self.addCleanup(server.shutdown)
        self.item.update(service='redis', port=server.server_address[1])
        self.assertEqual('ACESSO_CONFIRMADO', self.cli()[1]['results'][0]['state'])
        self.assertEqual([b'AUTH', b'PING', b'QUIT'], commands)


if __name__ == '__main__':
    unittest.main()
