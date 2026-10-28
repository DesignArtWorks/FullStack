import assert from 'node:assert/strict';
import test from 'node:test';
import { createRequire } from 'node:module';
import { createServer } from 'node:http';
import { once } from 'node:events';
import { dirname, join } from 'node:path';

const require = createRequire(import.meta.url);
const { JSDOM } = require('jsdom');
const createDOMPurify = require('dompurify');
const { z } = require('zod');

// RN-SCA99-001/002: inert marker only; jsdom never enables script execution.
for (const expression of ['${SCA_MARKER}', '{{SCA_MARKER}}', '<%SCA_MARKER%>']) {
  test(`RN-SCA99-002: shadow-root template fragments are scrubbed: ${expression}`, () => {
    const dom = new JSDOM('<!doctype html><body></body>');
    try {
      const doc = dom.window.document;
      const host = doc.createElement('div');
      doc.body.appendChild(host);
      const root = host.attachShadow({ mode: 'open' });
      const editorial = doc.createElement('span');
      editorial.textContent = 'Editorial text';
      root.appendChild(editorial);
      for (const character of expression) {
        root.appendChild(doc.createTextNode(character));
        root.appendChild(doc.createElement('object'));
      }
      createDOMPurify(dom.window).sanitize(host, { IN_PLACE: true, SAFE_FOR_TEMPLATES: true });
      root.normalize();
      assert.ok(!root.textContent.includes(expression), 'merged expression must not survive shadow sanitization');
      assert.ok(root.textContent.includes('Editorial text'), 'ordinary editorial text must remain');
    } finally { dom.window.close(); }
  });
}

for (const detached of ['parent', 'sibling']) {
  test(`RN-SCA99-002: IN_PLACE hook neutralizes a detached ${detached}`, () => {
    const dom = new JSDOM('<!doctype html><body><div id="root"><figure><img class="drop"><img id="retained" onerror="SCA_MARKER"></figure><p>Editorial</p></div></body>');
    try {
      const doc = dom.window.document;
      const dangerous = doc.getElementById('retained');
      const purifier = createDOMPurify(dom.window);
      purifier.addHook('afterSanitizeAttributes', node => {
        if (node.getAttribute?.('class') === 'drop') {
          if (detached === 'parent') node.parentNode.remove();
          else node.nextElementSibling.remove();
        }
      });
      purifier.sanitize(doc.getElementById('root'), { IN_PLACE: true });
      assert.equal(dangerous.getAttribute('onerror'), null);
      assert.equal(doc.querySelector('p').textContent, 'Editorial');
    } finally { dom.window.close(); }
  });
}

test('RN-SCA99-002: bounded Zod4 arrays reject before walking elements and preserve valid input', () => {
  let visited = 0;
  const schema = z.array(z.preprocess(value => { visited++; return value; }, z.string())).max(2);
  const rejected = schema.safeParse(Array(1000).fill('synthetic'));
  assert.equal(rejected.success, false);
  assert.equal(visited, 0, 'size bound must precede element parsing');
  assert.equal(rejected.error.issues.length, 1);
  assert.deepEqual(schema.parse(['a', 'b']), ['a', 'b']);
  assert.equal(visited, 2);
});

test('RN-SCA99-002/003: legacy middleware option validation remains compatible', () => {
  const Koa = require('koa');
  const sessionModule = require('koa-session');
  const createSession = sessionModule.default || sessionModule;
  const app = new Koa();
  app.keys = ['synthetic-test-key'];
  assert.equal(typeof createSession({ key: 'synthetic.session', maxAge: 60_000 }, app), 'function');
  assert.throws(() => createSession({ maxAge: 'not-a-valid-age' }, app));
  const { koaBody } = require('koa-body');
  assert.equal(typeof koaBody({ parsedMethods: ['POST', 'PUT'], jsonLimit: '1mb' }), 'function');
  assert.throws(() => koaBody({ parsedMethods: ['NOT_A_METHOD'] }));
});

test('RN-SCA99-002: Markdown rendering preserves editorial markup and escapes raw HTML', () => {
  const MarkdownIt = require('markdown-it');
  const html = new MarkdownIt().render('**Editorial**\n\n<script>SCA_MARKER</script>');
  assert.match(html, /<strong>Editorial<\/strong>/);
  assert.ok(!html.includes('<script>'));
  assert.ok(html.includes('&lt;script&gt;'));
});

test('RN-SCA99-002: redirects reach the destination without cross-origin credentials', async t => {
  const received = [];
  const destination = createServer((req, res) => { received.push(req.headers); res.end('editorial'); });
  destination.listen(0, '127.0.0.1');
  await once(destination, 'listening');
  const source = createServer((_req, res) => {
    res.writeHead(302, { location: `http://127.0.0.1:${destination.address().port}/content` }); res.end();
  });
  source.listen(0, '127.0.0.1');
  await once(source, 'listening');
  t.after(() => Promise.all([source, destination].map(server => new Promise((resolve, reject) => {
    server.close(error => error ? reject(error) : resolve()); server.closeAllConnections();
  }))));
  const response = await new Promise((resolve, reject) => {
    const request = require('follow-redirects').http.get(`http://127.0.0.1:${source.address().port}/start`, {
      headers: { authorization: 'Bearer synthetic-test-credential', cookie: 'synthetic=test' },
    }, res => {
      let body = ''; res.setEncoding('utf8'); res.on('data', chunk => { body += chunk; });
      res.on('end', () => resolve({ status: res.statusCode, body })); res.on('error', reject);
    });
    request.setTimeout(5000, () => request.destroy(new Error('redirect test timed out')));
    request.on('error', reject);
  });
  assert.deepEqual(response, { status: 200, body: 'editorial' });
  assert.equal(received.length, 1);
  assert.equal(received[0].authorization, undefined);
  assert.equal(received[0].cookie, undefined);
});

// RN-SCA99-003: scoped reachability evidence, NOT remediation of Zod3 itself.
test('RN-SCA99-003: real Strapi body middleware never validates HTTP arrays as Zod3 options', async t => {
  const { default: middlewares } = await import('../config/middlewares.ts');
  const entry = middlewares.find(item => item === 'strapi::body' || item?.name === 'strapi::body');
  assert.ok(entry, 'configured body middleware must exist');
  const config = typeof entry === 'string' ? {} : entry.config;
  const bodyRequire = createRequire(require.resolve('koa-body'));
  const legacy = bodyRequire('zod');
  const { KoaBodyMiddlewareOptionsSchema } = require('koa-body/lib/types');
  const expectedMethods = KoaBodyMiddlewareOptionsSchema.parse(config).parsedMethods;
  const seen = [];
  const parseArray = legacy.ZodArray.prototype._parse;
  legacy.ZodArray.prototype._parse = function(input) {
    seen.push(Array.isArray(input.data) ? [...input.data] : input.data);
    return parseArray.call(this, input);
  };
  t.after(() => { legacy.ZodArray.prototype._parse = parseArray; });
  const { body } = require(join(dirname(require.resolve('@strapi/core')), 'middlewares/body.js'));
  const app = new (require('koa'))();
  app.use(body(config, { strapi: { plugin: () => undefined } }));
  app.use(ctx => { ctx.body = { received: ctx.request.body }; });
  const server = app.listen(0, '127.0.0.1');
  await once(server, 'listening');
  t.after(() => new Promise((resolve, reject) => {
    server.close(error => error ? reject(error) : resolve()); server.closeAllConnections();
  }));
  const untrusted = Array(256).fill('synthetic-request-element');
  for (const payload of [untrusted, { parsedMethods: untrusted }]) {
    const response = await fetch(`http://127.0.0.1:${server.address().port}/content`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify(payload), signal: AbortSignal.timeout(5000),
    });
    assert.equal(response.status, 200);
    assert.deepEqual((await response.json()).received, payload);
  }
  assert.equal(seen.length, 2, 'Strapi creates the options parser on each request');
  for (const parsed of seen) assert.deepEqual(parsed, expectedMethods, 'only static parsedMethods reaches the legacy array schema');
});
