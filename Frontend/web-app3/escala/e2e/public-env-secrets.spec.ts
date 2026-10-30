import { expect, test } from '@playwright/test';
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const root = process.cwd();
const forbidden = /NEXT_PUBLIC_[A-Z0-9_]*(SECRET|TOKEN|PASSWORD|PRIVATE|API_KEY)[A-Z0-9_]*/g;

function sourceFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((entry) => {
    const path = join(dir, entry);
    if (statSync(path).isDirectory()) return sourceFiles(path);
    return /\.(ts|tsx|mjs|js)$/.test(entry) ? [path] : [];
  });
}

test.describe('Variáveis públicas do frontend', () => {
  test('nenhuma variável NEXT_PUBLIC_ carrega segredo, token ou senha', () => {
    const files = [...sourceFiles(join(root, 'src')), join(root, '.env.example'), join(root, 'playwright.config.ts')];
    const offenders = files.flatMap((file) => {
      const matches = readFileSync(file, 'utf8').match(forbidden) ?? [];
      return matches.map((name) => `${file}: ${name}`);
    });

    expect(offenders).toEqual([]);
  });
});
