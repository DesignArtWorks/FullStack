import { test, expect, type BrowserContext } from '@playwright/test';
import sharp from 'sharp';

const origin = 'http://127.0.0.1:3100';
let pixel: Buffer;
test.beforeAll(async () => {
  pixel = await sharp({ create: { width: 1, height: 1, channels: 3, background: '#fff' } }).png().toBuffer();
});

async function authenticate(context: BrowserContext, email: string, companySlug: string) {
  const csrf = await (await context.request.get('/api/auth/csrf')).json();
  await context.request.post('/api/auth/callback/credentials', { form: {
    csrfToken: csrf.csrfToken, email, password: 'E2e-password-123!', companySlug, json: 'true',
  } });
  expect((await (await context.request.get('/api/auth/session')).json()).user.email).toBe(email);
}

test('avatar is visible to owner and active colleagues, never another tenant or anonymous visitors', async ({ browser, context }) => {
  await authenticate(context, 'alice@tenant-a.test', 'tenant-a');
  const upload = await context.request.post('/api/bff/avatar/upload', {
    headers: { Origin: origin }, multipart: { file: { name: 'pixel.png', mimeType: 'image/png', buffer: pixel } },
  });
  expect(upload.status()).toBe(200);
  const { url } = await upload.json();
  const own = await context.request.get(url);
  expect(own.status()).toBe(200);
  expect(own.headers()['cache-control']).toContain('no-store');
  expect(own.headers()['content-type']).toBe('image/webp');
  for (const [email, company] of [['bob@tenant-b.test', 'tenant-b'], ['carol@tenant-a.test', 'tenant-a']]) {
    const other = await browser.newContext({ baseURL: origin });
    try {
      await authenticate(other, email, company);
      const response = await other.request.get(url, { headers: { 'X-Tenant-ID': 'aaaaaaaa-0000-0000-0000-000000000001' } });
      expect(response.status()).toBe(company === 'tenant-a' ? 200 : 404);
      if (response.ok()) expect(await response.body()).toEqual(await own.body());
    } finally { await other.close(); }
  }
  const anonymous = await browser.newContext({ baseURL: origin });
  try { expect((await anonymous.request.get(url)).status()).toBe(401); }
  finally { await anonymous.close(); }
});

test('avatar mutations require Origin and legacy public/private aliases fail closed', async ({ context }) => {
  await authenticate(context, 'alice@tenant-a.test', 'tenant-a');
  const invalidOrigins: Record<string, string>[] = [{}, { Origin: 'https://evil.test' }];
  for (const headers of invalidOrigins) {
    const response = await context.request.post('/api/bff/avatar/upload', { headers, multipart: { file: { name: 'pixel.png', mimeType: 'image/png', buffer: pixel } } });
    expect(response.status()).toBe(403);
  }
  for (const url of [
    '/api/bff/avatar/files/10000000-0000-0000-0000-000000000001-11111111-1111-1111-1111-111111111111.webp',
    '/uploads/avatars/legacy.jpeg', '/uploads/%61vatars/legacy.png',
    '/_next/image?url=%2Fuploads%2Favatars%2Flegacy.jpeg&w=64&q=75',
  ]) expect((await context.request.get(url)).status()).toBe(404);
});
