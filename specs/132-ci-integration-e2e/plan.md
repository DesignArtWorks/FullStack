# Plano — correção limitada da PR #132

Spec: [spec.md](spec.md). Dossiê e autorização: [interview.md](interview.md), P03. Constituição 1.0.0. Modo por fases.

## Contexto técnico e estrutura

Branch existente refactor/software-fundamentals, head inicial 46bc536. Desenvolvido no worktree limpo da PR, preservando rebase/conflitos do checkout principal. Atualização normal com origin/develop 79bfcbf antes da correção, sem reescrever histórico. Exceção de nomenclatura herdada: atualização da PR existente autorizada, sem renomear branch nem alegar fluxo original conforme.

Arquivos: .github/workflows/backend-integration.yml; scripts/ci/check-integration-reports.py e test-integration-reports.py; Frontend/web-app3/escala/e2e/bff-contract.spec.ts; quatro documentos desta spec. Python usa somente stdlib; NextAuth/Playwright existentes. Nenhum código Java ou runtime frontend alterado. Dívida arquitetural original e pendências da PR permanecem.

## Constitution Check inicial e antes da atualização da PR

| ID | Resultado no recorte / evidência |
| --- | --- |
| CON-01 | PASS: mesmos diretórios oficiais; somente CI/teste/docs. |
| CON-02 | N/A: sem regra ou dependência arquitetural de runtime. |
| CON-03 | PASS no desenho: callback NextAuth real, tenant explícito validado pelo mock; sessão/dados B e negação A exigidos. Execução em tasks. |
| CON-04 | N/A: nenhuma regra operacional nova. |
| CON-05 | PASS: dados sintéticos, sem schema/PII/secrets novos. |
| CON-06 | N/A: contratos/OpenAPI/comunicações inalterados. |
| CON-07 | N/A: sem alteração visual/claim; fluxo visual A ainda coberto. |
| CON-08 | PENDING até comandos/checks reais; FR-001/002/003 ligados a testes/código/commit/PR em tasks. |
| CON-09 | Parcial: worktree limpo e develop incorporada por merge normal; branch herdada fora do padrão. Atualizar PR #132; nenhum merge final autorizado. |

Este check aplica-se à correção, não certifica a refatoração original. CON-08/09 não são declarados integralmente concluídos sem evidência remota/revisão.

## Execução e pontos humanos

H1: aprovado por P03 após diagnóstico, arquivos/escopo e proposta apresentados. Implementar verificador XML estrito e controles negativos; corrigir sessão B sem afrouxar isolamento; validar; publicar o mesmo recorte na PR já autorizada. H2: apresentar primeira fatia concluída e resultados para revisão de Wemerson; sem merge até revisão/checks. Publicação da correção para revisão já autorizada, não será confundida com aceite H2.

## Risco e rollback

Principal risco: enfraquecer gate ou mascarar isolamento. Controles negativos e 403 explícito mitigam. Reverter apenas commit desta correção via PR restaura defeitos de CI; sem migration. Docker/health/Swagger local N/A para o recorte: nenhum backend runtime alterado; CI remoto continuará executando integração real de toda a PR.
