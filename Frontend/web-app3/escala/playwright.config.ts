import { defineConfig, devices } from '@playwright/test';
import { randomBytes } from 'node:crypto';

const appUrl = 'http://127.0.0.1:3100';
const backendUrl = 'http://127.0.0.1:8180';
const nextAuthSecret = randomBytes(32).toString('hex');

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? 'line' : 'list',
  expect: { timeout: 15_000 },
  use: {
    baseURL: appUrl,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'off',
  },
  outputDir: 'test-results/playwright',
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command: 'node e2e/support/mock-backend.mjs',
      url: `${backendUrl}/actuator/health`,
      reuseExistingServer: !process.env.CI,
      timeout: 30_000,
      env: { ...process.env, E2E_BACKEND_PORT: '8180' },
    },
    {
      command: 'node node_modules/next/dist/bin/next dev --hostname 127.0.0.1 --port 3100',
      url: `${appUrl}/api/health`,
      reuseExistingServer: !process.env.CI,
      timeout: 120_000,
      env: {
        ...process.env,
        API_BASE_URL: backendUrl,
        NEXT_INTERNAL_API_BASE_URL: backendUrl,
        NEXTAUTH_URL: appUrl,
        NEXT_PUBLIC_APP_URL: appUrl,
        ['NEXTAUTH_SECRET']: nextAuthSecret,
        NEXT_PUBLIC_COMPANY_SLUG: 'tenant-a',
        NEXT_PUBLIC_RECAPTCHA_ENABLED: 'false',
        NEXT_PUBLIC_GOOGLE_AUTH_ENABLED: 'false',
      },
    },
  ],
});
