# E2E de segurança do frontend

A suíte Playwright em `Frontend/web-app3/escala/e2e` valida as garantias da ADR-008 no Chromium: cookie HttpOnly/contrato de sessão, logout, ausência de Bearer do browser para o BFF, proteção Origin/CSRF e negação cross-tenant.

Os usuários, tenants, JWTs e senhas são exclusivamente sintéticos. O mock local existe apenas no processo de teste e simula a decisão autoritativa do Spring; ele não integra o bundle de produção.

## Execução local

No frontend oficial:

```bash
pnpm install --frozen-lockfile
pnpm exec playwright install chromium
pnpm run test:e2e
```

Traces são coletados somente no primeiro retry. Screenshots aparecem apenas em falha; nenhum payload de autenticação é anexado pelo teste.
