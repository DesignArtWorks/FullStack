# Tarefas — <nome>

Spec: `<numero>-<slug>` | Issue: #<numero> | Plano: <link> | Constituição: <versão>
Dossiê: `interview.md` da pasta da spec | Modo: **por fases** | Fase autorizada: <origem/escopo>

## Formato e execução

`- [ ] T005 [P] [US1] Descrição — FR/RN: IDs; Dep.: IDs; Arquivo: caminho; Prova: comando/cenário; Aceite: resultado esperado.`

ID é único na spec; referência externa usa `<spec>:T005`. [US1] liga à história; [P] é opcional e só indica independência de arquivos/decisões, não delegação automática. Cada tarefa deve citar arquivo/símbolo e comando que demonstra conclusão; revisão documental pode usar verificação de links/diff/estrutura. Aceite esperado e resultado real são campos distintos.

Estados: TODO, IN_PROGRESS, BLOCKED, DONE. Checkbox só marca DONE com evidência. Implement não decide regra nova. Falha ou decisão ausente da spec bloqueia o trecho dependente; investigação independente autorizada pode continuar.

Tarefa atômica produz mudança verificável, cabe no recorte autorizado e não exige nova decisão. Se misturar regras independentes, arquivos conflitantes ou muitas provas, decompor por decisão/regra; se só repete comando sem critério novo, agrupar com a tarefa que comprova. Não impor tamanho/tempo fixo nem criar burocracia por checkbox.

## Fase 1 — Setup

- [ ] **T001 — Explorar e isolar.** FR: <IDs>. Dep.: nenhuma. Arquivos: <fontes>. Prova: git status, fetch e base SHA; leitura dos testes. Aceite: issue/branch/base e impacto registrados, trabalho local preservado.
- [ ] **T002 — Conduzir ou reabrir entrevista.** FR: <IDs>. Dep.: T001. Arquivo: interview.md. Prova: gate do roteiro, respostas/canônicas/origens e confirmação humana. Aceite: recorte validado, sem decisão bloqueadora; não simular conversa.

**Checkpoint C1:** ambiente de análise disponível e gate socrático do recorte validado. Comandos/evidências: <reais>.

## Fase 2 — Foundational

- [ ] **T003 — Consolidar spec/plan e base das histórias.** FR: <IDs>. Dep.: T002. Arquivos: <lista>. Prova: Constitution Check inicial e revisão dos contratos/dados/testes. Aceite: desenho concreto para revisão H1 e tarefas sem decisão implícita.

**Parada H1 — antes de alterar código existente:** apresentar plano, arquivos, impacto, contratos/risco e testes previstos. Responsável: <nome>. Resultado: PENDING. Evidência de aprovação do recorte: <mensagem/decisão real>. Se nenhum código existente é alterado, registrar N/A com motivo. Não alterar arquivo dependente até a aprovação; autorização anterior concreta vale sem nova pergunta.

**Checkpoint C2:** base pronta para histórias e H1 atendido quando aplicável. Dependências/decisões pendentes: <IDs>.

## Fase 3 — US1 (P1), primeira fatia/MVP

- [ ] **T004 [US1] — Escrever/reusar verificação relevante.** FR/RN: <IDs>. Dep.: T003/H1. Arquivo: <teste>. Prova: <comando>. Aceite: cenário de regra/limite/negativo verificável.
- [ ] **T005 [US1] — Observar falha pertinente antes da correção.** FR/RN: <IDs>. Dep.: T004. Arquivo: <teste>. Prova: <comando/log sanitizado>. Aceite: falha prova o comportamento ausente, não erro de ambiente. N/A justificado para documentação ou comportamento já coberto; não criar falha artificial.
- [ ] **T006 [US1] — Implementar fatia completa.** FR/RN: <IDs>. Dep.: T005. Arquivos: <domínio/portas/adapters/DTO/BFF/UI conforme plano>. Prova: <comandos>. Aceite: US1 funciona isoladamente, sem novo acoplamento e com validação backend.

**Checkpoint C3:** US1 demonstrável; diff e verificações pertinentes disponíveis.

**Parada H2 — após o MVP:** apresentar resultado, comandos/resultados, diff, riscos e pendências. Responsável: <nome>. Resultado: PENDING. Aguardar aprovação humana para a próxima fase. Commit/push/PR de revisão pode registrar o resultado produzido, mas não autoriza outra história ou merge.

## Fase 4 — US2 e histórias seguintes, quando existirem

Decompor cada história aprovada em verificação, implementação e demonstração independente. Atribuir novos IDs T/US e dependências após H2; não adicionar requisito para preencher esta fase. Se não existir outra história, marcar N/A; o checkpoint humano do MVP continua obrigatório.

**Parada H3 — em qualquer fase, decisão fora da spec:** registrar PROV-ID, pergunta/dono, comportamento afetado e tarefa bloqueada. Voltar à entrevista e atualizar spec/plan antes do trecho dependente. Nenhuma escolha de permissão, tenant, limite ou resultado fica a cargo do implementador por omissão.

## Fase 5 — Polish e entrega

- [ ] **T007 — Validação/revisão final.** Dep.: última história e H2. Arquivos: <lista>. Prova: <gates reais>, Constitution Check final, docs/OpenAPI/diff. Aceite: resultado verificável, riscos e rollback registrados.
- [ ] **T008 — Commits/push e PR de integração.** Dep.: T007. Prova: SHAs/URL reais e controles de revisão. Aceite: escopo coeso, checks/pendências informados. PR de revisão do MVP pode ter sido aberto em H2; não confundir abertura com aprovação.
- [ ] **T009 — Integrar no destino autorizado.** Dep.: T008, revisão humana e gates do head. Prova: PR/checks/merge reais. Aceite: develop integrado; main apenas conforme escopo autorizado e fluxo oficial.
- [ ] **T010 — Registrar entrega/encerramento.** Dep.: T009 e integrações exigidas. Prova: inventário, comandos/resultados, riscos/rollback e pushes. Aceite: distinguir integrado, promovido e publicado; não fechar issue antecipadamente.

Renumerar ao gerar tarefas concretas, preservando IDs já usados quando atualizar uma spec existente. Não executar fase posterior por silêncio, timeout ou teste verde. Instrução "execute somente fase X e pare" limita a execução.

## Ledger de execução e commits

| Tarefa / fase / US | Status | Regra / FR | Prova executada / resultado | Commit SHA | PR / bloqueio |
| --- | --- | --- | --- | --- | --- |
| T001 / Setup | TODO | <IDs> | Não executado | — | — |

Registrar SHA corrente no próximo commit ou PR; após squash, preservar IDs e registrar SHA final de merge no PR/issue.

## Registro de revisão humana

| Checkpoint | Responsável | Artefato/head revisado | Decisão humana / data / origem | Escopo liberado |
| --- | --- | --- | --- | --- |
| H1 | <nome> | <plano/arquivos> | PENDING ou N/A justificado | <recorte> |
| H2 | <nome> | <MVP/diff/resultados> | PENDING | <próxima fase> |
| H3, se necessário | <nome> | <PROV-ID/spec> | PENDING até decisão | <tarefa> |

## Validações

| Verificação | Comando / ambiente / data | Resultado | Evidência / pendência |
| --- | --- | --- | --- |
| <gate> | <real> | PENDING | <link/log sanitizado> |

Expected não é actual. Skipped/falho/não executado não é sucesso. Testes podem ser executados pelo agente e revisados pelo humano; não exigir que o humano digite comandos. Mudança documental usa gates documentais e CI obrigatório.
