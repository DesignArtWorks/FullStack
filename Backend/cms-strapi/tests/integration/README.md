# Executar os testes reais de identidade do CMS (#103)

A suíte `npm run test:security:integration` inicia o Strapi real e grava fixtures.
Use **um PostgreSQL novo e descartável**, nunca bancos do Escala ou CMS existentes.
O job `CMS / Build` fornece esse banco automaticamente no CI, sem Docker socket.

1. Use Node 22 e instale o lockfile com `npm ci`.
2. Configure `DATABASE_CLIENT=postgres`, `DATABASE_HOST`, `DATABASE_PORT`,
   `DATABASE_USERNAME`, `DATABASE_PASSWORD`, e um `DATABASE_NAME` terminado em
   `_identity_test`. Não configure `DATABASE_URL`.
3. Gere chaves sintéticas para `APP_KEYS`, `API_TOKEN_SALT`, `ADMIN_JWT_SECRET`,
   `TRANSFER_TOKEN_SALT`, `ENCRYPTION_KEY` e `JWT_SECRET`. Não reutilize secrets reais.
4. Configure `NODE_ENV=production`, `STRAPI_CRON_ENABLED=false` e
   `CMS_IDENTITY_TEST_DISPOSABLE=true`.
5. Execute `npm run test:security && npm run build && npm run test:security:integration`.
6. Remova somente o banco/containers descartáveis criados para essa execução.

O nome do banco e a flag são guardrails; quem executa continua responsável por
provisionar um banco novo. A suíte não apaga bancos nem reutiliza primeiro admin.
Se o ambiente estiver incorreto ou uma dependência indisponível, falha; não faz skip.

São quatro cenários reais: 54 tentativas de rotas legadas com/sem JWT, rejeição de
JWT assinado com IDs coincidentes e snapshots persistidos, admin editorial, e
API token com publicação/leitura pública. A contagem TAP inclui também o teste
pai (5 testes). Tokens inválidos e tentativa de mutação anônima são controles
adicionais. 404 e 405 significam rota legada indisponível; 405 vem do catch-all
GET estático. 401/403 são exigidos na tentativa de usar credencial final contra
editorial/admin. Nenhum token ou senha real deve ser enviado ao teste.

Certificados corporativos, quando necessários para npm local, são configuração
externa de download; não adicionar certificados, bypass TLS ou caminhos locais
a manifests, workflow ou Dockerfile.
