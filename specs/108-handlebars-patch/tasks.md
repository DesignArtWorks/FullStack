# Tarefas — correção transitiva do CMS

Spec: `108-handlebars-patch` | Issue: #108 | [spec](spec.md) | [plan](plan.md) | [dossiê](interview.md) | Constituição 1.0.0
Modo: por fases. H1 aprovado por Wemerson antes do patch, Q-HB001; H2 pendente. Registro documental completo posterior à implementação, sem aprovação retrospectiva inventada.

## Fases e tarefas atômicas

Setup:

- [x] T001 — Analisar dependência, avisos, consumidores e impacto; arquivos manifest/lock/workflow; prova: leitura e investigação independente; aceite: dois caminhos transitivos identificados, sem contrato/domínio novo.

Foundational:

- [x] T002 — Registrar H1 do recorte e preservar autorização; arquivo interview.md; prova: Q-HB001 e resposta literal; aceite: alteração limitada aprovada. Documento escrito após revisão, aprovação real anterior.

US-HB1/MVP:

- [x] T003 — Escrever testes antes do patch e observar RED, RN-SCA108-HB001/002/003, FR-HB001/002/003; arquivo tests/handlebars-security.test.mjs; dep. T001/T002; prova: node --test com 4.7.9 isolado; aceite: quatro negativos falham, controle legítimo passa.
- [x] T004 — Fixar versão e lock, RN-SCA108-HB001/002; arquivos package.json/package-lock.json; dep. T003; prova: npm ls handlebars --all; aceite: ambos consumidores em 4.7.10, nenhuma outra versão atualizada.
- [x] T005 — Verificar correção/compatibilidade, RN-SCA108-HB001–003; testes e build CMS; dep. T004; prova: npm ci, npm run test:security, geração Strapi/node-plop, npm run build e Trivy; aceite: 13 testes/0 skips, build e SCA aprovados.
- [x] T006 — Revisão independente somente leitura; arquivos diff e evidências; dep. T005; prova: parecer e cinco testes focados reproduzidos; aceite: sem defeito confirmado, limites da reprodução explícitos.

Checkpoint: MVP técnico demonstrado; H2 exige revisão humana do diff, dossiê e evidências. Nenhuma US2 nova neste recorte. H3 interrompe qualquer decisão fora da spec; não surgiu decisão de negócio nova.

Polish/entrega:

- [ ] T007 — Completar artefatos/check constitucional/commit/PR e checks do head efetivo; arquivos spec/plan/tasks/interview; dep. T006; prova: diff --check, links/matriz, SHA/PR/runs; aceite: registros coerentes e oito checks do último head success. IN_PROGRESS: documentação regularizada após revisão; CI anterior passa, novo commit exige CI próprio.
- [ ] T008 — Revisão H2 e integração no destino autorizado; dep. T007/H2; prova: aprovação humana e merge/checks reais; aceite: nunca inferir merge/main/encerramento por testes verdes. BLOCKED até Wemerson aprovar.

## Ledger canônico de execução/commits

| Tarefa | Estado | Evidência / commit / PR |
| --- | --- | --- |
| T001 | DONE | Consumidores generators/node-plop e avisos oficiais; plan |
| T002 | DONE | H1 real, Q-HB001; proveniência do dossiê, arquivo acrescentado posteriormente |
| T003 | DONE | RED 4.7.9: 4 negativos falham, 1 controle passa; evidência persistida |
| T004 | DONE | 1bc1b6a; override/lock e npm ls 4.7.10 nos dois caminhos; #127 |
| T005 | DONE | Node22, 13 testes, geração/build/SCA local PASS; run 37928863795, oito jobs success |
| T006 | DONE | Revisão independente: cinco testes reproduzidos; build/SCA conferidos por evidência, sem reprodução completa após container encerrar |
| T007 | IN_PROGRESS | #127 e #126; artefatos completos neste commit; SHA/CI atualizado no corpo dos PRs para evitar autorreferência |
| T008 | BLOCKED | H2/merge não realizados; Wemerson |

## Revisão humana, riscos e rollback

H1: APPROVED, Wemerson, 2026-10-08, “Aprovar correção e validação”, apenas patch/validação e documentação necessária. H2: PENDING, revisão do MVP e documentos; não autoriza nova história/merge por silêncio. H3: nenhuma decisão nova tomada; pendências de #106 ficam fora deste recorte. Rollback reintroduz CVEs, exigindo novo tratamento do gate; não remover checks/supressões. CA corporativa somente em downloads locais, sem arquivo no repositório.
