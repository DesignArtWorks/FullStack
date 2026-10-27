const assert = require('node:assert/strict');
const { createHmac, randomBytes } = require('node:crypto');
const test = require('node:test');
const { createStrapi, compileStrapi } = require('@strapi/strapi');

// RN-SEC103-001/003: real HTTP and persistence, never a mocked authentication layer.
// The runner must provide a NEW disposable database; refuse application databases.
test('RN-SEC103-001/003: CMS rejects end-user identity and preserves editorial access', { timeout: 180_000 }, async t => {
  assert.equal(process.env.CMS_IDENTITY_TEST_DISPOSABLE, 'true', 'explicit disposable-database opt-in required');
  assert.equal(process.env.DATABASE_CLIENT, 'postgres');
  assert.equal(process.env.DATABASE_URL, undefined, 'connection-string overrides are forbidden in this test');
  assert.match(process.env.DATABASE_NAME || '', /_identity_test$/);
  assert.equal(process.env.STRAPI_CRON_ENABLED, 'false');
  assert.ok(process.env.JWT_SECRET, 'synthetic legacy signing key required');
  const strapi = createStrapi(await compileStrapi());
  t.after(async () => { await strapi.destroy(); });
  await strapi.start();
  const base = `http://127.0.0.1:${strapi.config.get('server.port')}`;
  async function request(path, { method = 'GET', token, body } = {}) {
    return fetch(base + path, {
      method, redirect: 'manual',
      headers: { ...(body ? { 'content-type': 'application/json' } : {}), ...(token ? { authorization: `Bearer ${token}` } : {}) },
      ...(body ? { body: JSON.stringify(body) } : {}),
      signal: AbortSignal.timeout(15_000),
    });
  }
  const password = randomBytes(24).toString('base64url') + 'aA1!';
  const admin = await strapi.admin.services.user.createFirstAdmin({
    firstname: 'Synthetic', lastname: 'Editor', email: 'editor@example.invalid', password,
  });
  const role = await strapi.db.query('plugin::users-permissions.role').findOne({ where: { type: 'authenticated' } });
  // Both independent identity tables deliberately contain the same numeric ID.
  const user = await strapi.plugin('users-permissions').service('user').add({
    username: 'synthetic-editor', email: 'plugin@example.invalid', password,
    confirmed: true, blocked: false, provider: 'local', role: role.id,
  });
  const legacy = await strapi.db.query('api::user-account.user-account').create({ data: {
    id: user.id, username: 'synthetic-legacy', email: 'legacy@example.invalid', password, theme: 'light',
  } });
  assert.equal(legacy.id, user.id);
  // Grant the plugin identity a real editorial write permission: denial must not
  // merely be the accidental absence of role permissions.
  await strapi.db.query('plugin::users-permissions.permission').create({ data: {
    action: 'api::article.article.create', role: role.id,
  } });
  const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url');
  const payload = `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode({ id: legacy.id, iat: Math.floor(Date.now() / 1000), exp: Math.floor(Date.now() / 1000) + 300 })}`;
  const signature = createHmac('sha256', process.env.JWT_SECRET).update(payload).digest('base64url');
  const legacyJwt = `${payload}.${signature}`;
  const before = {
    user: await strapi.db.query('plugin::users-permissions.user').findOne({ where: { id: user.id } }),
    legacy: await strapi.db.query('api::user-account.user-account').findOne({ where: { id: legacy.id } }),
    users: await strapi.db.query('plugin::users-permissions.user').count(),
    accounts: await strapi.db.query('api::user-account.user-account').count(),
    articles: await strapi.db.query('api::article.article').count(),
  };
  await t.test('RN-SEC103-001: anonymous and valid legacy JWT cannot reach removed auth, theme or CRUD routes', async () => {
    const paths = [
      ['POST', '/api/user-accounts/login'], ['POST', '/api/user-accounts/register'],
      ['POST', '/api/user-accounts/forgot-password'], ['POST', '/api/user-accounts/reset-password'],
      ['POST', '/api/auth/local'], ['POST', '/api/auth/local/register'],
      ['POST', '/api/auth/forgot-password'], ['POST', '/api/auth/reset-password'],
      ['GET', '/api/auth/email-confirmation'], ['POST', '/api/auth/send-email-confirmation'],
      ['GET', '/api/connect/google'], ['GET', '/api/auth/google/callback'],
      ['POST', '/api/auth/refresh'], ['POST', '/api/auth/logout'],
      ['PATCH', '/api/users/theme'], ['PATCH', `/api/users/${user.id}`],
      ['PUT', `/api/users/${user.id}`], ['DELETE', `/api/users/${user.id}`],
      ['GET', '/api/users/me'], ['GET', '/api/users'], ['GET', `/api/users/${user.id}`],
      ['POST', '/api/users'], ['GET', '/api/user-accounts'], ['POST', '/api/user-accounts'],
      ['GET', `/api/user-accounts/${legacy.documentId}`], ['PUT', `/api/user-accounts/${legacy.documentId}`],
      ['DELETE', `/api/user-accounts/${legacy.documentId}`],
    ];
    for (const token of [undefined, legacyJwt]) {
      for (const [method, path] of paths) {
        const response = await request(path, { method, token, ...(['GET', 'DELETE'].includes(method) ? {} : { body: {
          id: user.id, identifier: 'legacy@example.invalid', username: 'attacker',
          email: 'attacker@example.invalid', password, theme: 'dark',
        } }) });
        assert.ok([404, 405].includes(response.status), `${method} ${path}: removed route must be unavailable, got ${response.status} (credential=${Boolean(token)})`);
      }
    }
  });
  await t.test('RN-SEC103-002: valid legacy JWT cannot mutate editorial data or fall back to public', async () => {
    for (const token of [undefined, legacyJwt, legacyJwt + 'invalid']) {
      const response = await request('/api/articles', { method: 'POST', token, body: { data: { title: 'Unauthorized', slug: 'unauthorized' } } });
      assert.ok([401, 403].includes(response.status), `editorial mutation denied: ${response.status}`);
    }
    for (const token of [legacyJwt, legacyJwt + 'invalid']) {
      const response = await request('/api/articles', { token });
      assert.ok([401, 403].includes(response.status), `supplied credential must not become public fallback: ${response.status}`);
    }
    assert.deepEqual(await strapi.db.query('plugin::users-permissions.user').findOne({ where: { id: user.id } }), before.user);
    assert.deepEqual(await strapi.db.query('api::user-account.user-account').findOne({ where: { id: legacy.id } }), before.legacy);
    assert.equal(await strapi.db.query('plugin::users-permissions.user').count(), before.users);
    assert.equal(await strapi.db.query('api::user-account.user-account').count(), before.accounts);
    assert.equal(await strapi.db.query('api::article.article').count(), before.articles);
  });
  await t.test('RN-SEC103-003: CMS admin login and authorized content management remain usable', async () => {
    const response = await request('/admin/login', { method: 'POST', body: { email: admin.email, password } });
    assert.equal(response.status, 200);
    const body = await response.json();
    const token = body.data.token;
    assert.ok(token, 'admin login must issue its separate credential');
    assert.equal((await request('/admin/users/me', { token })).status, 200);
    const created = await request('/content-manager/collection-types/api::article.article', {
      method: 'POST', token, body: { title: 'Admin synthetic article', slug: 'admin-synthetic-article' },
    });
    assert.ok([200, 201].includes(created.status), `admin editorial create: ${created.status}`);
    const denied = await request('/admin/users/me', { token: legacyJwt });
    assert.ok([401, 403].includes(denied.status), 'legacy JWT must never become CMS admin');
  });
  await t.test('RN-SEC103-003: independent API token can publish content and anonymous readers see it', async () => {
    const apiToken = await strapi.admin.services['api-token'].create({
      name: 'identity-test-editorial', type: 'full-access', kind: 'content-api', lifespan: null,
    });
    const response = await request('/api/articles?status=published', {
      method: 'POST', token: apiToken.accessKey,
      body: { data: { title: 'Published synthetic article', slug: 'published-synthetic-article' } },
    });
    assert.equal(response.status, 201, 'editorial API token must remain usable');
    const published = await response.json();
    const publicRead = await request(`/api/articles/${published.data.documentId}`);
    assert.equal(publicRead.status, 200);
    assert.equal((await publicRead.json()).data.title, 'Published synthetic article');
    assert.equal((await request('/api/articles', { token: apiToken.accessKey })).status, 200);
  });
});
