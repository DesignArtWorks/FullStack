# Tarefas — <nome>

Spec: `<numero>-<slug>` | Issue: #<numero> | Plano: <link> | Constituição: <versão>

## Regras de execução

Status: TODO, IN_PROGRESS, BLOCKED, DONE. Checkbox só marca DONE com aceite e evidência. IDs locais à spec; citar `<spec>:T001` em referências externas. `[P]` só indica independência de arquivos/decisões. Nenhuma regra nova é decidida durante implement; atualizar spec/plan quando houver descoberta.

## Sequência

- [ ] **T001 — Analisar e isolar.** Dep.: nenhuma. FR: <IDs>. Remotes/develop, issue, branch, trabalho local, arquivos/testes. Aceite: base SHA e impacto registrados.
- [ ] **T002 — Aprovar spec/plan.** Dep.: T001. FR: <IDs>. Constitution Check e pendências. Aceite: invariantes/contratos/tenant definidos.
- [ ] **T003 — Verificar comportamento esperado.** Dep.: T002. FR/RN: <IDs>. Regras, limites/negativos e falha pertinente antes da correção quando possível. Documento usa revisão própria. Aceite: evidência apropriada.
- [ ] **T004 — Implementar domínio/caso de uso.** Dep.: T003. FR/RN: <IDs>. Arquivos: <lista>. Aceite: regra pura/portas e testes aprovados.
- [ ] **T005 — Implementar adapters/contratos/fluxo.** Dep.: T004. FR/RN: <IDs>. Arquivos: <lista>. Aceite: DTO, tenant, migration, BFF/UI conforme plano.
- [ ] **T006 — Validar e revisar.** Dep.: T005. Aceite: testes/gates pertinentes, docs/OpenAPI, diff, riscos/rollback e Constitution Check final.
- [ ] **T007 — Commit/push e PR develop.** Dep.: T006. Aceite: SHA/URL reais, escopo coeso, checks e pendências informados.
- [ ] **T008 — Integrar develop.** Dep.: T007. Aceite: aprovação/checks do head e merge protegido registrados.
- [ ] **T009 — Promover main.** Dep.: T008. Aceite: diff acumulado revisado, PR/checks/aprovação/merge registrados.
- [ ] **T010 — Fechar entrega.** Dep.: T009. Aceite: inventário, comandos/resultados, riscos/rollback e pushes/integrações confirmados.

Decompor conforme risco; declarar N/A sem tarefas fictícias. Cada tarefa funcional deve incluir arquivo/símbolo e cenário de aceite concreto.

## Ledger de execução

| Tarefa | Status | Regra / FR | Evidência real / resultado | Commit SHA | PR / bloqueio |
| --- | --- | --- | --- | --- | --- |
| T001 | TODO | <IDs> | Não executado | — | — |

SHA corrente será registrado no próximo commit ou no PR. Depois de squash, preservar IDs no corpo e registrar SHA final do merge no PR/issue.

## Validações e entrega

| Verificação | Ambiente / comando / data | Resultado | Evidência / pendência |
| --- | --- | --- | --- |
| <gate> | <real> | PENDING | <link/log sanitizado> |

Nunca copiar expected como actual. Skipped/falho/não executado não é sucesso. PR aberto, merge develop e promoção main são estados distintos.
