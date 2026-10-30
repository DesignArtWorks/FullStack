# Especificação — CI de integração e E2E

Spec: 132-ci-integration-e2e. Referência: PR #132 (não representa nova issue). Data: 2026-10-30. Dossiê: [interview.md](interview.md). Aprovação do recorte: P03.

## Problema, ator e valor

O mantenedor precisa distinguir falha real de integração de relatório TXT vazio de container JUnit, e executar a verificação cross-tenant com credenciais coerentes. Fontes: interview:P01/P02.

## Requisitos e aceite

- FR-001: validar XML Surefire agregado, aceitando testes @Nested mesmo com TXT externo vazio. Fonte P01; verificador e controle positivo.
- FR-002: rejeitar ausência de relatórios de integração, XML inválido/incompleto, zero testes, contagens inconsistentes, falhas, erros e skips. Fonte P04; controles negativos.
- FR-003: autenticar Bob via NextAuth com tenant-b, comprovar identidade da sessão, resposta exclusiva B e 403/ACCESS_DENIED ao solicitar empresa A. Fonte P02/P04; teste bff-contract.

## Contratos, invariantes e escopo

Nenhuma mudança REST/BFF, DTO, OpenAPI, banco, permissão ou seleção de tenant em produção. Dados sintéticos somente. Backend/mock continua validando identidade/tenant. Nenhuma nova dependência; Python padrão disponível no runner Ubuntu. Sem incremento de retry/timeout ou supressão de teste. Sem merge/main/deploy.

## Verificação

Oito controles unitários do verificador, leitura de relatórios reais existentes, suíte Playwright completa, lint/typecheck/build frontend, diff e CI remoto. Limites das evidências serão registrados em tasks/PR.
