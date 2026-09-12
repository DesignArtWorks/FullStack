import { createServer } from 'node:http';

const port = Number(process.env.E2E_BACKEND_PORT ?? 8180);
const users = {
  'alice@tenant-a.test': { id: '10000000-0000-0000-0000-000000000001', tenant: 'tenant-a' },
  'bob@tenant-b.test': { id: '20000000-0000-0000-0000-000000000002', tenant: 'tenant-b' },
};

const json = (response, status, body, headers = {}) => {
  response.writeHead(status, { 'content-type': 'application/json', ...headers });
  response.end(JSON.stringify(body));
};

const tokenFor = (user) => {
  const header = Buffer.from(JSON.stringify({ alg: 'none', typ: 'JWT' })).toString('base64url');
  const payload = Buffer.from(JSON.stringify({ sub: user.id, companySlug: user.tenant, exp: Math.floor(Date.now() / 1000) + 3600 })).toString('base64url');
  return `${header}.${payload}.e2e-signature`;
};

const tenantFromAuthorization = (authorization = '') => {
  const token = authorization.replace(/^Bearer\s+/i, '');
  try {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString()).companySlug;
  } catch {
    return null;
  }
};

createServer(async (request, response) => {
  const url = new URL(request.url ?? '/', `http://127.0.0.1:${port}`);
  const correlationId = request.headers['x-correlation-id'] ?? 'mock-correlation';

  if (url.pathname === '/actuator/health') return json(response, 200, { status: 'UP' });

  if (request.method === 'POST' && url.pathname === '/api/v1/auth/authenticate') {
    let body = '';
    for await (const chunk of request) body += chunk;
    const credentials = JSON.parse(body || '{}');
    const user = users[credentials.email];
    if (!user || credentials.password !== 'E2e-password-123!' || credentials.companySlug !== user.tenant) {
      return json(response, 401, { code: 'INVALID_CREDENTIALS' }, { 'x-correlation-id': correlationId });
    }
    return json(response, 200, {
      token: tokenFor(user),
      user: { id: user.id, username: credentials.email.split('@')[0], email: credentials.email, roles: ['USER'], companySlug: user.tenant, theme: 'SYSTEM' },
    }, { 'x-correlation-id': correlationId });
  }

  const tenant = tenantFromAuthorization(request.headers.authorization);
  const companyMatch = url.pathname.match(/^\/api\/v1\/companies\/([^/]+)$/);
  if (request.method === 'GET' && companyMatch) {
    const requestedTenant = companyMatch[1];
    if (!tenant) return json(response, 401, { code: 'AUTHENTICATION_REQUIRED' }, { 'x-correlation-id': correlationId });
    if (requestedTenant !== tenant) return json(response, 403, { code: 'ACCESS_DENIED', message: 'Acesso negado.' }, { 'x-correlation-id': correlationId });
    return json(response, 200, { id: tenant, name: tenant }, { 'x-correlation-id': correlationId });
  }

  if (request.method === 'POST' && url.pathname === '/api/v1/check-in') {
    if (!tenant) return json(response, 401, { code: 'AUTHENTICATION_REQUIRED' }, { 'x-correlation-id': correlationId });
    return json(response, 200, { tenant, accepted: true }, { 'x-correlation-id': correlationId });
  }

  return json(response, 404, { code: 'NOT_FOUND' }, { 'x-correlation-id': correlationId });
}).listen(port, '127.0.0.1');
