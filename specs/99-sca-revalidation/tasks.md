# Tarefas — 99-sca-revalidation

Spec [spec.md](spec.md); plano [plan.md](plan.md); dossiê [interview.md](interview.md). P01 autoriza SCA/patch compatível; H2 pendente.

- [x] T001 — FR-001: ler governança, issues e manifests; fetch/branch. Base 28488b6; registry consultado em 16/11.
- [x] T002 — FR-001: baseline Trivy sem exceções PASS: Maven/CMS/frontend, zero High/Critical. DB 16/11/2026; [research.md](research.md).
- [x] T003 — FR-002: duas exceções removidas; nenhum patch compatível novo necessário/disponível. Trivy com `.trivyignore.yaml` vazio PASS para os dois locks. Manifests/locks preservados; diff/links PASS.
- [ ] T004 — FR-003: apresentar diff/resultado H2, CON pré-PR, publicar evidências autorizadas; commit/PR/merge distintos.

Não há commit/PR desta fatia ainda. #106 e #109 não são concluídas por estas tarefas. O gate global #99 continua PENDING.

## Validações e limites

- Baseline SCA: PASS, três componentes, sem nenhuma supressão.
- Política versionada após edição: PASS, CMS/frontend, mesma DB; nenhuma vulnerabilidade suprimida.
- Markdown links e `git diff --check`: PASS.
- Gitleaks v8.24.2/digest do CI: harness de padrões/exceções e harness de história/refs/erros PASS; história completa de origin/develop e HEAD 28488b6 PASS, zero findings não excepcionados. Falhas impressas pelos controles negativos são esperadas; exit final 0.
- Runtime/testes de aplicação/lint/build/Docker health: N/A para esta fatia sem alteração de aplicação, manifests ou locks; continuam obrigatórios no gate global #99 e no CI do PR.
- `.gitignore` recebeu alteração concorrente para ignorar um env local; preservada e excluída da entrega. Nenhum conteúdo do env foi lido ou publicado.
- H2 e decisões #106/#109 aguardam resposta humana. Não executar fase dependente por silêncio.
