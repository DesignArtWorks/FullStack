import { expect, test, type Page } from '@playwright/test';

const password = 'E2e-password-123!';

async function login(page: Page, email: string) {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page).toHaveURL(/\/dashboard/);
}

async function bffGet(page: Page, path: string) {
  return page.evaluate(async (target) => {
    const response = await fetch(target);
    return { status: response.status, body: await response.json().catch(() => null) };
  }, path);
}

test.describe('Contrato BFF: escala, trocas e ponto', () => {
  test.describe('sem sessão', () => {
    test('rotas privadas do BFF respondem 401 e não chegam ao backend', async ({ request }) => {
      for (const path of ['/api/bff/schedules/swap-requests', '/api/bff/scheduling/cycles']) {
        const response = await request.get(path);
        expect(response.status(), path).toBe(401);
      }
    });
  });

  test.describe('com sessão válida', () => {
    test('lista trocas e ciclos apenas do tenant da sessão', async ({ page }) => {
      await login(page, 'alice@tenant-a.test');

      const swaps = await bffGet(page, '/api/bff/schedules/swap-requests');
      expect(swaps.status).toBe(200);
      expect(swaps.body).toEqual([{ id: 'swap-tenant-a', status: 'PENDING', tenant: 'tenant-a' }]);

      const cycles = await bffGet(page, '/api/bff/scheduling/cycles');
      expect(cycles.status).toBe(200);
      expect(cycles.body[0].tenant).toBe('tenant-a');
    });

    test('usuários de tenants diferentes nunca recebem dados um do outro', async ({ context }) => {
      // 132-ci-integration-e2e:FR-003: authenticate B explicitly; the UI defaults to tenant A.
      const csrf = await (await context.request.get('/api/auth/csrf')).json();
      await context.request.post('/api/auth/callback/credentials', {
        form: { csrfToken: csrf.csrfToken, email: 'bob@tenant-b.test', password, companySlug: 'tenant-b', json: 'true' },
      });
      const session = await (await context.request.get('/api/auth/session')).json();
      expect(session.user.email).toBe('bob@tenant-b.test');

      const swaps = await context.request.get('/api/bff/schedules/swap-requests');
      expect(swaps.status()).toBe(200);
      expect(await swaps.json()).toEqual([{ id: 'swap-tenant-b', status: 'PENDING', tenant: 'tenant-b' }]);

      const crossTenant = await context.request.get('/api/bff/companies/tenant-a');
      expect(crossTenant.status()).toBe(403);
      expect((await crossTenant.json()).code).toBe('ACCESS_DENIED');
    });

    test('check-in ignora tenant enviado no corpo e usa o da sessão', async ({ page, baseURL }) => {
      await login(page, 'alice@tenant-a.test');

      const result = await page.evaluate(async () => {
        const response = await fetch('/api/bff/check-in', {
          method: 'POST',
          headers: { 'content-type': 'application/json' },
          body: JSON.stringify({ latitude: -23.55, longitude: -46.63, deviceFingerprint: 'e2e', tenantId: 'tenant-b' }),
        });
        return { status: response.status, body: await response.json() };
      });

      expect(baseURL).toBeTruthy();
      expect(result.status).toBe(200);
      expect(result.body.tenant).toBe('tenant-a');
    });

    test('mutações em trocas rejeitam Origin externa', async ({ page, context }) => {
      await login(page, 'alice@tenant-a.test');

      const response = await context.request.post('/api/bff/schedules/swap-requests', {
        headers: { Origin: 'https://evil.test' },
        data: { originalShiftId: 'x' },
      });

      expect(response.status()).toBe(403);
    });
  });
});
