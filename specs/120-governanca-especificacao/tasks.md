# Tarefas — Adoção da governança por especificação

Spec: `120-governanca-especificacao` | Issue: #120 | Constituição: 1.0.0
Plano: [plan.md](plan.md) | Branch: `docs/issue-120-constitution-plan`

## Controle

TODO, IN_PROGRESS, BLOCKED e DONE são estados de execução; checkbox marcado requer aceite e evidência. IDs T/FR são locais à spec. Esta sequência executa a adoção documental; futuras features usam os templates e o roteiro técnico do plano.

## Sequência executável

- [x] **T001 — Atualizar base e isolar.** FR-007; CON-09. Dep.: nenhuma. Aceite: fetch, SHA base e branch dedicada preservando checkout principal. Evidência: base `08d0dcdb8646804b9a8913b5ea94d36dc3199890`; worktree `.worktrees/issue-120`.
- [x] **T002 — Inventariar arquitetura e evidências.** FR-002; CON-02/08. Dep.: T001. Arquivo: analysis.md. Aceite: fronteiras, exemplos, manifests/workflow e divergências documentados, sem declarar testes executados.
- [x] **T003 — Consolidar constituição.** FR-001; todos os CON. Dep.: T002. Arquivo: constitution.md. Aceite: princípios verificáveis, fontes, versão e emenda separada.
- [x] **T004 — Especificar e planejar.** FR-003; todos os CON. Dep.: T003. Arquivos: spec.md, plan.md. Aceite: FR/US/SC, impacto, arquivos, Constitution Check, validação e rollback.
- [x] **T005 — Definir tarefas e ledger.** FR-004/005; CON-08/09. Dep.: T004. Arquivo: tasks.md. Aceite: IDs, dependências e evidência real; entrega externa aberta.
- [x] **T006 — Criar templates reutilizáveis.** FR-006; CON-08. Dep.: T004. Arquivos: `.specify/templates/*.md`. Aceite: princípios no plano e rastreabilidade nos três templates.
- [x] **T007 — Integrar instruções do repositório.** FR-006; CON-08/09. Dep.: T006. Arquivos: guia e AGENTS. Aceite: ponte para fonte única, sem instalar CLI.
- [x] **T008 — Validar documentos e Constitution Check final.** FR-001 a FR-007. Dep.: T005/T007. Aceite: links/IDs, diff, scanner de segredos e escopo aprovados; resultados registrados.
- [x] **T009 — Criar commits coesos e push.** FR-005/007; CON-09. Dep.: T008. Aceite: arquivos previstos, issue/spec/TIDs no corpo, SHAs reais e push confirmado. Evidência: b35705a e f53cebe enviados a origin em 2026-10-08.
- [x] **T010 — Abrir PR para develop.** FR-005/007; CON-09. Dep.: T009. Aceite: URL, diff, comandos/resultados, riscos/rollback e gates pendentes explícitos. Evidência: [PR #121](https://github.com/DesignArtWorks/FullStack/pull/121).
- [ ] **T011 — Concluir revisão/CI e merge develop.** FR-007; CON-09. Dep.: T010. Aceite: checks verdes do head, aprovação exigida e merge protegido. PR aberto não conclui este passo.
- [ ] **T012 — Promover main e encerrar issue.** FR-007; CON-09. Dep.: T011. Aceite: diff revisado, PR develop/main, gates/aprovação/merge/push, inventário final e fechamento somente ao término.

## Ledger de execução e commits

| Tarefas | Estado | Evidência | Commit / PR |
| --- | --- | --- | --- |
| T001 | DONE | Fetch + worktree branch baseada em origin/develop | Base 08d0dcd |
| T002–T004 | DONE | analysis, constitution, spec, plan produzidos | b35705a |
| T005–T007 | DONE | tasks, templates, guia e ponte AGENTS | f53cebe46f845cfe845b9210aeb8baadc5d0b706 |
| T008 | DONE | Links/IDs, staged diff e scanner aprovados em 2026-10-08; revisão CON-01 a CON-09 | Resultado documental |
| T009 | DONE | Dois commits coesos enviados a origin em 2026-10-08 | b35705a; f53cebe |
| T010 | DONE | PR aberto para develop com inventário, validações, riscos e rollback | [PR #121](https://github.com/DesignArtWorks/FullStack/pull/121) |
| T011–T012 | TODO | Dependem de PR/checks/revisão e promoção | — |

SHA do commit que atualiza este ledger será informado no PR; não pode ser armazenado no próprio commit. Após squash, acrescentar SHA final no PR/issue ou atualização posterior.

## Registro de validação

| Verificação | Resultado | Evidência / motivo |
| --- | --- | --- |
| Links locais e IDs/princípios | PASS | PowerShell: links de nove documentos resolvidos; CON-01 a CON-09 presentes em constituição/plano/template; 2026-10-08 |
| git diff --check | PASS | git diff --cached --check, sem erros; 2026-10-08 |
| Scanner de segredos versionados | PASS | ./scripts/security/check-versioned-secrets.ps1 após stage: Versioned secret scan passed; 2026-10-08 |
| Constitution Check final | PASS documental | Revisão dos nove princípios e diff: somente dez arquivos Markdown previstos; entrega/CI seguem pendentes |
| Testes/lint/typecheck/build local de runtime | N/A | Somente Markdown; aplicação/contratos inalterados |
| Backend Docker/health/Swagger/OpenAPI local | N/A | Backend não alterado |
| CI do PR / revisão / merge | PENDING | Não dispensado pelo escopo documental |
| Promoção main / fechamento da issue | PENDING | Somente após T011/T012 |

## Revisão antes do merge em develop

Em 2026-10-08, a revisão do PR #121 levou a dois ajustes: tabela final explícita CON-01 a CON-09 no plano/template e ficha completa de aceite por regra no template da spec. O apontamento do ledger já estava corrigido em `ab9cbfa`. O CI desse head passou no run `37788331334`; o novo head exige revalidação. A ordem humana atual autoriza merge em develop; main não foi autorizado nesta etapa.
