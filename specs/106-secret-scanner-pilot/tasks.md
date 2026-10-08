# Tarefas — #106 por fases

Spec: 106-secret-scanner-pilot | Issue: #106 | [Plano](plan.md) | [Dossiê](interview.md) | [Controles](controls.md)
Base: 0efcc18f0dc3a8cc53974de363d3f6128ea73cac | Branch: security/issue-106-secret-scanner-pilot
Fase autorizada: US1, H1 aprovado por Wemerson em 2026-10-08. H2/US2/limpeza PENDING.

## Setup

- [x] **T001 — Investigar quadro, fontes e base.** FR-001/009. Dep.: nenhuma. Arquivos: workflow/scripts/docs/AGENTS. Prova: quadro autenticado Ready #103/#106/#109, In progress #99; fetch e worktree base 0efcc18. Aceite: piloto executável escolhido, trabalho local de UI preservado.
- [x] **T002 — Conduzir e fechar entrevista real.** FR-001 a FR-010. Dep.: T001. Arquivo: interview.md. Prova: 27 respostas reais, Q027 “sim” ao fechamento; fontes/abertas e refinamentos registrados. Aceite: dossiê concluído, sem converter fechamento em H1/entrevista plenamente validada.
- [x] **T003 — Consolidar/verificar documentos para H1.** FR-008/009. Dep.: T002. Arquivos: os cinco desta pasta. Prova: PowerShell confirmou links locais, cinco seções e checks CON inicial/final; git diff --check e scanner versionado aprovados em 2026-10-08. Aceite: proposta e pendências concretas para Wemerson; nenhum script/workflow alterado.

Checkpoint C1: dossiê e desenho revisáveis; gate implementação PENDING.

**H1 — revisão humana antes de código existente:** Wemerson revisa spec/plan/controls, arquivos, riscos e testes US1. Estado APPROVED US1 em 2026-10-08. Aprovar US1 não libera US2, rewrite de histórico, autenticação externa, promoção main ou fechamento #99. Aprovação pode registrar recorte específico e condição existente sem pedir confirmação idêntica novamente.

## Foundational — somente após H1 US1

- [x] **T004 — Verificar scanner, refs e inventariar exceções.** RN-SEC106-001/002; FR-001/002/004. Dep.: T003/H1. Arquivos: research.md, .gitleaks*, relatório redigido. Prova: documentação oficial e inventário sem valores. Aceite: versão/digest/status/cobertura estabelecidos; ocorrências reais não classificadas como fictícias por conveniência.
- [x] **T005 — Preparar controles e observar falha pertinente.** RN-SEC106-001/002; FR-001/003. Dep.: T004. Arquivo: test-secret-scanner.sh. Prova: harness em repo sintético, incluindo segredo só no passado/refs e erro de execução. Aceite: falha distingue cobertura ausente/detecção esperada, sem ser erro de ambiente ou teste artificial.

Checkpoint C2: testes/refs prontos; findings do histórico real podem impedir integração, sem bloquear teste isolado independente. H3 para alteração de política/exceção.

## US1 (P1), MVP

- [ ] **T006 [US1] — Integrar scanner histórico ao job.** RN-SEC106-001/002; FR-001 a FR-004. Dep.: T005/H1. Arquivos: workflow, scan-secret-history.sh se necessário, controles. Prova: harness, refs/SHA, scan redigido e execução CI. Aceite: develop/head PR cobertos, erro não vira PASS, exceção específica, nenhum request externo de validade.
- [ ] **T007 [US1] — Demonstrar MVP e conciliar docs.** FR-008/009; SC-001 a SC-004/008/009. Dep.: T006. Arquivos: docs/secret-scanning-issue-106.md, docs/ci-gates.md, ledger. Prova: diff, harness/CI e docs coerentes com execução. Aceite: resultados reais e limitações, sem afirmar scanner limpo se findings permanecem.

Checkpoint C3: US1 demonstrável; integração ainda sujeita a finding/CI/revisão.

**H2 — após MVP:** Wemerson revisa resultado, diff, provas, riscos/pendências e PR se aberto. Estado PENDING. Testes verdes não substituem aceite. Não iniciar US2/rewrite/merge por silêncio.

## US2 e remediação — recortes condicionais, após H2

- [ ] **T008 — Preparar plano operacional de limpeza.** RN-SEC106-004; FR-005/007. Dep.: T004 e inventário. Arquivo: history-remediation-plan.md. Prova: refs/clones/backup privado/recovery/fingerprints, sem segredo. Aceite: AB-002 resolvida e autorização específica; preparar não é executar.
- [ ] **T009 — Resolver e desenhar contrato local.** RN-SEC106-003; FR-006. Dep.: H2, AB-001/003. Arquivo: contracts/local-validation.md. Prova: decisão humana e mapping por tipo/serviço/fontes. Aceite: estados/efeitos/limites/alvos e tratamento de signing keys definidos; H1 próprio antes da implementação.
- [ ] **T010 [US2] — Testar e implementar diagnóstico local.** RN-SEC106-003; FR-006. Dep.: T009/H1 próprio. Arquivo: tooling/testes conforme contrato aprovado. Prova: sandbox de sucesso/negado/indisponível e acesso real local quando autorizado. Aceite: histórico e .env ignorados, zero autenticação externa/valores em log/efeito de negócio; resultado inconclusivo não é credencial inválida.
- [ ] **T011 — Aplicar remediação histórica/registrar rotação no marco.** RN-SEC106-004; FR-005/007. Dep.: T008 e autorização operacional específica. Prova: plano aprovado/ensaio/scan real redigido/ref SHA; rotação depois #103/#106/#109, antes gate final #99. Aceite: nenhuma reescrita/bypass implícito; rotação não marcada antes de executar.

T011 coordena o marco de rotação, não cria dependência circular para concluir #106. Findings reais que impeçam o próprio gate exigem resolução específica anterior; aceitação temporária de validade local não é aprovação de allowlist real.

**H3:** AB-001/002/003/004 ou nova decisão ausente bloqueia somente tarefa dependente. Voltar ao diálogo antes de editar/operar esse trecho.

## Polish / entrega

- [ ] **T012 — Verificar push protection e gates finais.** FR-009/010. Dep.: história/recortes concluídos, H2. Arquivos: evidências, plan/tasks. Prova: configuração disponível ou AB-004 explícita, CON final, todos os jobs CI aplicáveis. Aceite: nenhuma pendência crítica escondida, riscos/rollback e limitações registrados.
- [ ] **T013 — Commit/push/PR no destino autorizado.** FR-009. Dep.: gates do recorte. Prova: SHA/PR reais; corpo cita issue/spec/T IDs. Aceite: PR develop protegido com revisão/checks do head, sem bypass. PR de revisão pode existir antes de H2 sem autorizar merge.
- [ ] **T014 — Registrar integração e handoff para #99.** FR-007/009. Dep.: T013/checks/revisão/escopo. Prova: merge SHA/CI/revisor e inventário na issue/PR. Aceite: não promover main, fechar #99 ou alegar deploy por esta entrega; fechar #106 só quando integrações/aceites exigidos cumpridos.

## Ledger

| Tarefa | Estado | Prova real | Commit / PR / bloqueio |
| --- | --- | --- | --- |
| T001 | DONE | Quadro/issues/fontes consultadas, base fetch e worktree criado | Base 0efcc18; sem commit novo |
| T002 | DONE | Diálogo Q001–Q027 e fechamento humano | interview.md, aprovação H1 pendente |
| T003 | DONE | Cinco documentos e verificações locais aprovadas em 2026-10-08 | Sem commit/push/PR nesta preparação; H1 PENDING |
| T004–T014 | TODO | Não executadas | H1/H2/H3 e dependências acima |

## Aprovações humanas

| Checkpoint | Dono | Artefato | Decisão / estado | Escopo |
| --- | --- | --- | --- | --- |
| Fechamento | Wemerson | Recorte Q027 | “sim”, 2026-10-08 | Dossiê e preparação do plano |
| H1 US1 | Wemerson | spec/plan/controls/tasks desta versão | APPROVED, resposta sim, 2026-10-08 | Scripts/workflow US1 |
| H2 | Wemerson | MVP/diff/resultados efetivos | PENDING | Próxima fase/integração conforme recorte |
| H1 US2 / limpeza | Wemerson | Contrato/plano específicos ainda não produzidos | PENDING | Nenhuma execução autorizada |

## Validação desta preparação

| Verificação | Resultado | Evidência |
| --- | --- | --- |
| Links, cinco seções, IDs e tabelas CON | PASS preparação | PowerShell: cinco arquivos com links resolvidos; cinco seções no dossiê; duas ocorrências CON-01 a CON-09 no plan; revisão da matriz de IDs |
| Diff / scanner versionado dos documentos | PASS preparação | git diff --check sem erro; check-versioned-secrets.ps1: Versioned secret scan passed; cinco arquivos em git add -N para inspeção, sem commit |
| Runtime/testes/lint/build/Docker | N/A | Apenas cinco Markdown novos; implementação ainda não iniciada |
| Scanner histórico/autenticação/rewrite/CI novo | NÃO EXECUTADO | Nenhuma alegação de PASS/limpeza/revogação |

Tasks é snapshot até seu último commit; resultados posteriores de aprovação/CI/merge são registrados no PR/issue com fonte real e conciliados na próxima atualização pertinente.

## Execução US1 — 2026-10-08

H1 aprovado por Wemerson, resposta literal “sim”, somente US1. T004 DONE: imagem v8.24.2/digest validada; 20 exceções inventariadas, 18 locais retiradas e duas sintéticas preservadas. T005 DONE: RED com segredo removido em clone depth=1 (0 em vez de 42), antes do wrapper; GREEN nos controles de padrões/exceção e refs independentes. T006 implementado localmente, CI remoto PENDING. T007 demonstração local concluída; H2 PENDING.

Scan real de origin/develop e da branch piloto na base 0efcc18: BLOCKED por findings. Não é PASS do histórico, não prova validade e não houve login/rewrite. Remediação não autorizada pela US1. Resultados finais serão registrados no PR de revisão.

Sintaxe sh e dois harnesses: PASS. PowerShell rastreado e diff --check: PASS. Runtime/backend Docker health/OpenAPI e builds locais N/A: scripts/CI/documentação, nenhum código de aplicação alterado. Os oito jobs remotos continuam obrigatórios; não alegar aprovação global até execução do head.

As tabelas de preparação anteriores são snapshots; este registro posterior prevalece para H1/T004/T005. T008–T012/T014 continuam pendentes. T013 autorizado somente para commit/push/PR draft de revisão, sem merge.

### Evidência local final

Histórico completo das duas refs na base 0efcc18: **16 findings**, BLOCKED (status interno 42). Nenhum valor foi impresso. Os 18 fingerprints removidos não equivalem à contagem final: cobertura/ref e deduplicação do scanner determinam as ocorrências detectadas. Harness final após relatório temporário sanitizado: PASS. Links Markdown e scanner PowerShell: PASS. H2 PENDING; CI remoto pendente.

## Remediação após o MVP — 2026-10-08

Este registro posterior prevalece sobre os snapshots anteriores para o recorte de remediação.
Wemerson: “Então vamos tratar as ocorrências e o obter o CI verde”. H2 libera preparação/correção/ensaio de limpeza; US2, rotação, merge e alteração de proteção não foram aprovados por esse pedido.

| Tarefa | Estado atual | Evidência |
| --- | --- | --- |
| T006/T007 | DONE MVP; integração bloqueada | PR #124; run 37818305235: seis outros jobs PASS, scanner 16 findings/42, Required Gate FAIL consequente |
| T008 | DONE preparação; publicação depende H3 | history-remediation-plan.md: refs/SHA, clone independente, leases/atomic, proteção, recuperação, demais refs/clones |
| T011 | PARTIAL, ensaio isolado PASS | 15 ocorrências removidas; uma fixture JWT sintética proposta como exceção exata; scanner completo sem findings não excepcionados; nenhuma ref remota modificada |
| T009/T010 | PENDING US2 | Sem autenticação de valores encontrados ou rotação |
| T012/T014 | PENDING | CI remoto do head final, manutenção/revisão e integração ainda não concluídos |

CMS: fallback de DATABASE_PASSWORD removido para PostgreSQL/MySQL; testes RED com duas falhas pertinentes, depois GREEN (sete testes de segurança no total); build Docker Node 22 PASS. Nenhum contrato REST, regra de tenant ou código backend/frontend alterado. A fixture JWT foi identificada como frase de testes de 41 bytes, exclusiva de application-test.yml nas árvores originais e não reutilizada nos .env locais verificados. Nova exceção específica proposta para revisão humana; nenhuma exclusão ampla.

Harnesses de padrões/exceções e história/refs/erros PASS; scanner completo do candidato develop 43a8f12 e entrega c4c1f3f PASS (0). Revisão assistiva independente PASS sem regressão concreta; não substitui aceite humano. Mapa/evidência sanitizados retidos em armazenamento gerenciado de segurança. Valores privados não publicados.

A stack oficial foi parada durante a sessão (containers exited por volta de 18:29 UTC, backend 137/OOMKilled false). Resposta local de CMS não comprova runtime do container parado. Build/testes do CMS corrigido foram executados isoladamente; não atribuir à aplicação corrigida uma falha de host/serviço não identificado.

H3 publicação PENDING: develop requer PR, Required Gate atualizado e conversas resolvidas, protege administradores e proíbe force push. Aprovar explicitamente a exceção de manutenção descrita no plano antes de operar. Não remover checks obrigatórios nem fazer merge do PR com CI vermelho. Remediação de main/demais refs/forks/caches/clones permanece fora do escopo.

### Autorização excepcional — somente este caso

Wemerson confirmou “SIM. MAS PARA SOMENTE ESTE CASO” em 2026-10-08, após revisão do plano concreto: exceção sintética e manutenção de develop/branch PR #124 com force push restrito, suspensão temporária de exigência de PR e restauração imediata, mantendo checks obrigatórios. H3 APPROVED exclusivamente para essa operação; não é política permanente nem autorização para outros PRs, main, rotação ou merge com CI vermelho.

Base sanitizada publicada em security/issue-106-history-maintenance (43a8f12e4774eccfe28ef61623485d4ab0fa90ff) apenas para validação CI prévia. Desenvolver a operação requer Required Gate aprovado nessa base, scanner estrito PASS, SHAs remotos ainda iguais aos esperados e restauração verificada da proteção. Estado da troca de refs/CI final registrado em evidência externa depois da operação.

## Publicação excepcional executada — 2026-10-08

Registro posterior que prevalece sobre estados pendentes anteriores. Wemerson aprovou a exceção somente para este caso e confirmou a identidade no GitHub. Publicação atômica com leases exatos concluída: develop b83673be1c69ee1786f18cc922f1427b37d917db e branch do PR #124 9e098443ab2d1d4c17c3dde2fa740af9af824850. main e demais referências preexistentes não foram alteradas.

A execução manual da base passou, mas o servidor rejeitou a primeira troca: workflow_dispatch não satisfaz checks obrigatórios. Como não há ancestral comum com a develop antiga, um gatilho push restrito à branch temporária security/issue-106-history-maintenance foi acrescentado à base saneada. O run [37832667534](https://github.com/DesignArtWorks/FullStack/actions/runs/37832667534) passou nos oito jobs. A entrega remove esse gatilho temporário; Required Gate e scanner permanecem obrigatórios.

O scanner estrito local passou na ancestralidade completa dos dois SHAs publicados. O run real de PR [37833171774](https://github.com/DesignArtWorks/FullStack/actions/runs/37833171774) passou nos sete jobs executores, incluindo o scan de ambos os históricos. A conclusão do Required Gate e dos heads posteriores é registrada no PR e nas evidências gerenciadas.

As proteções originais foram restauradas e comparadas: PR obrigatório, Required Gate de GitHub Actions, atualização de branch, resolução de conversas e aplicação a administradores; force push e deleções proibidos. Incidente da janela: a restrição ao ator selecionado não persistiu na interface; ao detectar, a regra inteira foi imediatamente restaurada. Não permanece permissão excepcional.

15 ocorrências históricas foram removidas; uma fixture JWT comprovadamente sintética foi aceita por fingerprint exato, somada às duas exceções sintéticas anteriores remapeadas. Nenhuma exclusão ampla de testes foi criada. O CMS deixou de ter fallback de senha PostgreSQL/MySQL e os sete testes de segurança/build passaram no CI real.

US2, rotação, merge do PR e fechamento de #106/#99 não são concluídos por esta manutenção. Histórico em main/outras branches/forks/caches e clones antigos permanece fora do escopo. Reaplicar trabalho antigo somente em clone/base saneada com novo scan; não mesclar o histórico antigo. Credenciais não foram rotacionadas neste desenvolvimento local.