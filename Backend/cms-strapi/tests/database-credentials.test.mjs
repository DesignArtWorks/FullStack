import assert from 'node:assert/strict';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import databaseConfig from '../config/database.ts';

// Strapi compiles its config to CommonJS; Node's test runner loads it as ESM.
globalThis.__dirname = fileURLToPath(new URL('../config/', import.meta.url));

// RN-SEC106-004: SQL credentials come from the environment, never a fallback.
for (const client of ['postgres', 'mysql']) {
  function config(password) {
    const env = (key, fallback) => {
      if (key === 'DATABASE_CLIENT') return client;
      if (key === 'DATABASE_PASSWORD' && password !== undefined) return password;
      return fallback;
    };
    env.int = (_key, fallback) => fallback;
    env.bool = (_key, fallback) => fallback;
    return databaseConfig({ env }).connection.connection;
  }

  test(`RN-SEC106-004: ${client} has no embedded password when env is absent`, () => {
    assert.equal(Boolean(config().password), false);
  });

  test(`RN-SEC106-004: ${client} uses the configured environment password`, () => {
    assert.equal(config('ci-only-database-fixture').password, 'ci-only-database-fixture');
  });
}
