import { expect, test, type BrowserContext, type Page } from '@playwright/test';

const password = 'E2e-password-123!';

async function login(page: Page, email = 'alice@tenant-a.test') {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page).toHaveURL(/\/dashboard/);
}

async function session(context: BrowserContext) {
  const response = await context.request.get('/api/auth/session');
  expect(response.ok()).toBeTruthy();
  return response.json();
}

test.describe('ADR-008: Auth, BFF e isolamento de tenant', () => {
  test('sessão não expõe bearer token e logout invalida cookie', async ({ page, context }) => {
    await login(page);
    const authenticated = await session(context);
    expect(authenticated.user.email).toBe('alice@tenant-a.test');
    expect(authenticated).not.toHaveProperty('accessToken');
    expect(authenticated.user).not.toHaveProperty('accessToken');
    expect(authenticated.user).not.toHaveProperty('token');

    await page.goto('/api/auth/signout');
    await page.getByRole('button', { name: /sign out/i }).click();
    await expect.poll(async () => (await session(context)).user).toBeUndefined();
  });

  test('browser não envia Bearer ao BFF e BFF autoriza recurso do próprio tenant', async ({ page }) => {
    await login(page);
    let browserAuthorization: string | undefined;
    page.on('request', (request) => {
      if (request.url().includes('/api/bff/companies/tenant-a')) browserAuthorization = request.headers().authorization;
    });
    const result = await page.evaluate(async () => {
      const response = await fetch('/api/bff/companies/tenant-a');
      return { status: response.status, body: await response.json() };
    });
    expect(result).toEqual({ status: 200, body: { id: 'tenant-a', name: 'tenant-a' } });
    expect(browserAuthorization).toBeUndefined();
  });

  test('nega acesso cruzado entre tenants mesmo com sessão válida', async ({ page }) => {
    await login(page);
    const result = await page.evaluate(async () => {
      const response = await fetch('/api/bff/companies/tenant-b');
      return { status: response.status, body: await response.json() };
    });
    expect(result.status).toBe(403);
    expect(result.body.code).toBe('ACCESS_DENIED');
    expect(result.body).not.toHaveProperty('companyId');
  });

  test('mutações BFF rejeitam Origin externa ou ausente', async ({ page, context }) => {
    await login(page);
    const external = await context.request.post('/api/bff/check-in', { headers: { Origin: 'https://evil.test' }, data: {} });
    expect(external.status()).toBe(403);
    const missing = await context.request.post('/api/bff/check-in', { data: {} });
    expect(missing.status()).toBe(403);
  });
});
