import assert from 'node:assert/strict';
import test from 'node:test';
import { createRequire } from 'node:module';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const Handlebars = createRequire(import.meta.url)('handlebars');

// RN-SCA108-HB001: constructor access must stay denied even with permissive methods.
test('RN-SCA108-HB001: lookup cannot expose Function constructor', () => {
  const template = Handlebars.compile('{{lookup (lookup fn "__proto__") "constructor"}}');
  assert.equal(template({ fn() {} }, { allowProtoMethodsByDefault: true }), '');
});

function malformedBlock() {
  const ast = Handlebars.parse('{{#if ok}}accepted{{/if}}');
  ast.body[0].program.blockParams = {
    length: '(()=>{throw new Error("HB_INJECTED")})()',
  };
  return ast;
}

// RN-SCA108-HB002: reject malformed ASTs before injected JavaScript can execute.
test('RN-SCA108-HB002: compile rejects malformed blockParams without executing it', () => {
  assert.throws(() => Handlebars.compile(malformedBlock())({ ok: true }),
    error => !String(error.message).includes('HB_INJECTED'));
});

test('RN-SCA108-HB002: precompile rejects malformed blockParams', () => {
  assert.throws(() => Handlebars.precompile(malformedBlock()));
});

test('RN-SCA108-HB002: precompile rejects malformed literal depth with stringParams', () => {
  const ast = Handlebars.parse('{{echo "text"}}');
  ast.body[0].params[0].depth = '0];throw new Error("HB_INJECTED");//';
  assert.throws(() => Handlebars.precompile(ast, { stringParams: true }));
});

// RN-SCA108-HB003: preserve legitimate templates, helpers, partials and valid ASTs.
test('RN-SCA108-HB003: ordinary generator template behavior remains usable', () => {
  const engine = Handlebars.create();
  engine.registerHelper('upper', value => value.toUpperCase());
  engine.registerPartial('item', '{{upper name}}');
  const source = '{{#each items}}{{> item}};{{/each}}';
  const context = { items: [{ name: 'alpha' }, { name: 'beta' }] };
  assert.equal(engine.compile(source)(context), 'ALPHA;BETA;');
  assert.equal(engine.compile(engine.parse(source))(context), 'ALPHA;BETA;');
  assert.match(engine.precompile(engine.parse(source)), /compiler/);
});

test('RN-SCA108-HB003: actual Strapi controller generation remains usable', async t => {
  const destination = await mkdtemp(join(tmpdir(), 'escala-handlebars-'));
  t.after(() => rm(destination, { recursive: true, force: true }));
  const { generate } = createRequire(import.meta.url)('@strapi/generators');
  const result = await generate('controller', {
    id: 'security-smoke', api: 'security-smoke', destination: 'api',
  }, { dir: destination });
  assert.equal(result.success, true);
  const controller = await readFile(join(destination,
    'src/api/security-smoke/controllers/security-smoke.js'), 'utf8');
  assert.match(controller, /security-smoke/);
  assert.ok(!controller.includes('{{'));
});
