# Estratégia de Deploy e Produção

Checklist complementar de go-live. A decisão vigente da primeira produção é AWS em `sa-east-1`, descrita na [ADR-006](adr-006-infraestrutura-minima-producao-aws.md). Segredos seguem a [ADR-005](adr-005-secrets-producao-aws.md). Exposição de portas segue a [ADR-007](adr-007-exposicao-servicos-producao.md).

`docker-compose.yml` é somente ambiente local. Este documento não autoriza produção em Compose, EC2 com Compose, EKS, Amplify, Vercel, App Runner ou Azure.

## 1. Baseline de produção

| Componente | Decisão vigente |
| --- | --- |
| Entrada | Route 53, CloudFront, AWS WAF e ACM. ALB público somente em HTTPS/443, com redirect de HTTP/80 |
| Frontend | ECS Fargate, task privada. O browser fala com o Next.js/BFF |
| Backend | ECS Fargate, task privada. Spring Boot é a fonte da verdade |
| CMS | ECS Fargate, task privada. Admin do Strapi por VPN, SSO ou allowlist. Mídia em S3 |
| Banco | RDS PostgreSQL Multi-AZ, sem IP público, criptografia em repouso |
| Cache | ElastiCache Valkey/Redis, sem IP público |
| Imagens | ECR por digest. Task definition não usa tag `latest` |
| Infra como código | Terraform por ambiente. Development, homologação e produção não compartilham VPC nem conta |
| Segredos | AWS Secrets Manager no runtime da task. SSM SecureString só para configuração sensível estável, conforme ADR-005 |
| Observabilidade | CloudWatch, CloudTrail e OpenTelemetry. Logs sem segredo |

Somente o ALB fica em subnet pública. Frontend, backend, Strapi, RDS e Redis não recebem IP público. `www` e `app` são os hosts públicos normais. `api` é consumida pelo BFF, com CORS restrito. `cms` não publica o admin na internet aberta. `assets` é CloudFront/S3, sem listagem de bucket.

## 2. DNS

- `www.escalas-saas.com.br`: site institucional e landing pages.
- `app.escalas-saas.com.br`: aplicação autenticada.
- `api.escalas-saas.com.br`: backend, preferencialmente atrás do BFF e do WAF.
- `cms.escalas-saas.com.br`: caminho administrativo restrito, não admin público.
- `assets.escalas-saas.com.br`: mídia via CloudFront/S3.

TLS termina no CloudFront e no ALB, com certificado ACM, TLS 1.2 ou superior e HSTS no frontend.

## 3. Checklist de lançamento

1. [ ] **Terraform:** aplicar a baseline da ADR-006 em homologação antes de produção.
2. [ ] **Backups:** RDS com backup automático e point-in-time recovery. Restore completo ensaiado, com RPO/RTO, conforme a issue #52.
3. [ ] **Rede:** Internet alcança só 443 no ALB. 8080 e 1337 não são públicos. Frontend não acessa RDS. Backend acessa RDS e Redis.
4. [ ] **Segredos:** nenhum valor em imagem, task definition, log ou repositório. Injeção pelo Secrets Manager.
5. [ ] **Logs:** CloudWatch sem segredo, com correlation id. Alarmes de target unhealthy, 5xx, RDS e Redis antes do go-live.
6. [ ] **CORS:** allowlist explícita. A API não aceita origem arbitrária.
7. [ ] **Health:** ALB usa `/actuator/health` no backend, `/api/auth/session` no frontend e `/admin` no Strapi.
8. [ ] **OpenAPI:** Swagger permanece desligado ou restrito fora do ambiente local.
9. [ ] **SSO:** `GOOGLE_CLIENT_ID` de produção com redirect HTTPS canônico. O segredo fica no BFF/backend, não no browser.
10. [ ] **Cookies:** sessão do NextAuth com `Secure` e `HttpOnly`. O JWT do Spring não vai ao browser.
11. [ ] **Billing:** webhook Stripe com assinatura e idempotência antes de cobrança real.
12. [ ] **LGPD:** consentimento, retenção e direitos do titular classificados no épico #36 antes de dado real de cliente.

## 4. Rollback

O rollback de aplicação troca a task definition para o digest estável anterior e espera o healthcheck. Ele não recria RDS, Redis, volumes nem dados.

Migrations de produção são aditivas e compatíveis por pelo menos uma versão. Migração irreversível exige backup/PITR validado e runbook específico. Não executar `down` automático em produção.
