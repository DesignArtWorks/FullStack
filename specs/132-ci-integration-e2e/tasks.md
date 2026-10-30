# Tarefas — 132-ci-integration-e2e

PR #132. [Plano](plan.md), [spec](spec.md), [fontes e autorização](interview.md). Sem nova issue fictícia. H1 aprovado P03; H2/merge pendentes.

- [x] T001 — Diagnosticar FR-001/FR-003 e preservar checkout principal. Run 41065747746, logs dos jobs 124096560860/124096561016; worktree da PR limpo; develop incorporada sem conflitos.
- [x] T002 — Registrar aprovação, escopo e CON-01 a CON-09. P03 e documentos desta pasta; nenhuma autorização de merge inventada.
- [x] T003 — FR-001/002: implementar verificador XML e oito controles. `python -B scripts/ci/test-integration-reports.py`: 8 PASS. Relatórios existentes: 20 suítes/83 testes aceitos; não representam nova execução backend. Fonte anterior RED: job rejeitou TXT vazio enquanto Maven aprovou 206 testes.
- [x] T004 — FR-003: substituir login incorreto de B por callback NextAuth com CSRF e companySlug; exigir sessão B, resposta B exata e 403 de A. Fonte RED: E2E do run anterior falhou no login de Bob; validação nova abaixo.
- [ ] T005 — Validar E2E completo, lint/typecheck/build e diff; registrar resultados reais.
- [ ] T006 — Publicar commit e atualizar PR #132 com SHA/checks efetivos e limites. Não significa merge develop/main.
- [ ] T007 — H2/revisão humana e eventual integração: fora do escopo de merge desta sessão; responsável Wemerson.

## Resultados

Controles Python: 8 PASS. Reproduzido sobre os relatórios locais anteriores: gate TXT rejeita 8 de 20 relatórios; XML aceita 20 suítes/83 testes reais, sem falhas/erros/skips. Não representa nova execução backend. Lint: PASS, 0 erros/77 warnings existentes. Diff desta correção: PASS. Typecheck/build em execução; E2E nativo bloqueado pela ausência do Chromium esperado, execução Linux em preparação. O AGENTS.md gerado automaticamente por next dev e caches não fazem parte do commit.

Runtime Java, health/Swagger/OpenAPI local N/A nesta correção sem alteração Java; checks remotos da PR continuam obrigatórios. Registro final de SHA/resultados no PR evita commit que alegue antecipadamente seu próprio SHA/publicação. T005/T006 permanecem abertos até comprovação no PR; nenhum PASS integral antecipado.
