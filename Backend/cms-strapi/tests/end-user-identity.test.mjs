import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readdir, readFile } from 'node:fs/promises';
import extend from '../src/extensions/users-permissions/strapi-server.ts';

test('removes end-user routes while preserving editorial admin routes', () => {
  const admin = { routes: [{ path: '/roles' }] };
  const plugin = extend({ routes: { admin, 'content-api': { routes: [{ path: '/auth/local' }, { path: '/users' }] } }, services: {} });
  assert.deepEqual(plugin.routes['content-api'].routes, []);
  assert.equal(plugin.routes.admin, admin);
});

test('neither legacy nor refresh-mode credentials can authenticate or issue tokens', () => {
  const plugin = extend({ routes: { 'content-api': { routes: [] } }, services: {} });
  const jwt = plugin.services.jwt();
  assert.equal(jwt.getToken({ request: { header: {} } }), null);
  for (const credential of ['Bearer synthetic.legacy.signature', 'bearer synthetic.refresh.signature', 'Basic synthetic']) {
    assert.throws(() => jwt.getToken({ request: { header: { authorization: credential } } }));
  }
  assert.throws(() => jwt.verify('synthetic.legacy.signature'));
  assert.throws(() => jwt.issue({ id: 1 }));
});

test('legacy custom API has no alternate authentication, theme or CRUD routes', async () => {
  const directory = new URL('../src/api/user-account/routes/', import.meta.url);
  assert.deepEqual(await readdir(directory), ['user-account.ts']);
  const source = await readFile(new URL('user-account.ts', directory), 'utf8');
  assert.match(source, /export default \{ routes: \[\] \}/);
});
