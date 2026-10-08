# Plano — Entrevista socrática e execução por fases

Spec: `122-entrevista-socratica` | Issue: #122 | Data: 2026-10-08
Branch: `docs/issue-122-entrevista-socratica`
Base: `origin/develop` em `91484273f9ea473ce38d0c6e3364d62c9a12315b`
Constituição: [1.0.0](../../.specify/memory/constitution.md)
Entradas: [spec.md](spec.md), [intake.md](intake.md) | Execução: [tasks.md](tasks.md)

## Análise e implementação proposta

PR #121 foi integrado após correção dos templates e CI verde. O repositório já usa constitution/spec/plan/tasks e uma skill de entrega que lê AGENTS. O novo roteiro será lido a partir de AGENTS, guia, prompts e templates; não é necessário criar skill, alterar configuração global ou instalar Spec Kit.

Fonte externa: texto e imagens enviados por Wemerson; tratá-los como referência de método. A resposta real de preferência fixou execução por fases. A adoção tem uma história documental completa: roteiro, dossiê, exemplo e pontos de entrada. Sem US2 inventada para preencher fases.

## Arquivos e finalidade

Technical Context desta adoção: Markdown no monorepo existente; verificações PowerShell/Git e scanner versionado. Nenhuma dependência ou linguagem runtime nova. Plataforma local Windows/PowerShell e GitHub CI vigente. `research.md`, `data-model.md` e `contracts/`: N/A, sem escolha de fornecedor, dados ou contrato novo. Quickstart está coberto pelos prompts, roteiro e comandos de verificação deste plano; não criar arquivo duplicado. Nenhuma meta numérica de performance do produto foi criada.

| Arquivo | Mudança |
| --- | --- |
| `docs/ai/entrevista-socratica.md` | Prompt executável pelo agente, fases/categorias, proveniência e gate |
| `docs/ai/exemplo-intervencao-socratica.md` | Recorte Vetor e dossiê didático incompleto, sem dados reais |
| `.specify/templates/interview-template.md` | Cinco seções, histórico/fontes, critérios e abertas com dono |
| `AGENTS.md` | Leitura obrigatória antes de implementar e modo por fases |
| `.specify/templates/spec-template.md` | Origem no dossiê e prontidão da entrevista |
| `.specify/templates/plan-template.md` | Gate e registro humano H1/H2/H3 |
| `.specify/templates/tasks-template.md` | Setup/Foundational/US1/histórias/Polish, IDs/prova/checkpoints |
| `docs/ai/indice.md`, `docs/ai/prompts-escala.md` | Descoberta do roteiro e prompts de entrevista/fase |
| `docs/desenvolvimento-orientado-especificacao.md` | Fluxo entrevista → proposta → spec/plan/tasks → execução |
| `specs/122-entrevista-socratica/{intake,spec,plan,tasks}.md` | Origem real, requisitos, desenho e execução desta adoção |
| `specs/120-governanca-especificacao/tasks.md` | Evidência do CI/revisão/merge concluídos, main pendente |

Contrato REST/eventos, persistência, autorização/tenant, UX runtime e dependências: N/A, porque o diff é exclusivamente documental. Sem dados pessoais além dos nomes de responsáveis já usados no projeto; nenhum segredo deve entrar nos artefatos.

## Gate socrático da adoção

Esta issue cria a política futura e registra sua origem em intake, sem afirmar que cinco fases e seis categorias foram conduzidas retroativamente. Após adoção, toda implementação deverá executar o roteiro e ter dossiê válido do recorte. O gate desta fase documental é a revisão humana do MVP no H2, não uma entrevista fictícia preenchida pelo agente.

| Checkpoint | Artefato e critério | Responsável / estado |
| --- | --- | --- |
| H1 | Nenhum código existente alterado; proposta/arquivos documentados | N/A justificado: somente Markdown |
| H2 | Roteiro, template, exemplo e pontos de entrada completos; diff/validações para revisão | Wemerson / PENDING |
| H3 | Decisão ausente da spec volta ao responsável | Nenhuma bloqueadora conhecida; aplicar se descoberta |

Modo aprovado: por fases (PROV-004). Preparar commit/push e PR para revisão do MVP é registro concreto da fase produzida; não constitui aprovação da integração ou da fase seguinte.

## Constitution Check inicial

| Princípio | Desenho / evidência | Resultado / pendência |
| --- | --- | --- |
| CON-01 | Fronteiras/runtime preservados; só docs | PASS |
| CON-02 | Sem alteração de camadas/estrutura; roteiro remete à arquitetura oficial | PASS |
| CON-03 | Não altera identidade/tenant; perguntas pertinentes constam do template | PASS |
| CON-04 | Gate impede decisões implícitas e mantém regras originais | PASS no desenho |
| CON-05 | Proveniência e minimização; sem dados/migrations/segredos | PASS no desenho |
| CON-06 | Sem novo REST/evento; contratos continuam no plano | PASS |
| CON-07 | Exemplo fictício explicitamente rotulado; sem claims novos | PASS |
| CON-08 | PROV→FR→US→tarefas→artefatos/verificações | PASS no desenho |
| CON-09 | Base pós-merge e branch isolada; checkpoints humanos e entrega real | PASS no desenho; H2/PR/CI pendentes |

Nenhuma emenda à constituição. A política operacional aplica princípios CON-04/08/09 por instrução explícita do usuário.

## Fases e provas

Setup: validar base/merge e fontes. Foundational: registrar origem/spec/plan. US1/MVP: roteiro/template/exemplo, vínculos e tarefas. Preparar diff/verificações e PR para H2. Após revisão humana, continuar Polish/integração conforme autorização. Main não faz parte deste pedido.

| Requisito | Tarefas | Artefatos / prova |
| --- | --- | --- |
| FR-001/002/003 | T004 | roteiro + template; conferir contrato, fases/categorias e cinco seções |
| FR-004/005 | T005/T006 | AGENTS, templates, guia/prompts; conferir gate, IDs/provas e H1/H2/H3 |
| FR-006 | T004 | exemplo didático rotulado; sem diálogo adicional simulado |
| FR-007 | T001/T007 | GitHub CI run/merge + ledger anterior |
| FR-008 / SC-005 | T006 | Contexto/estrutura/apoios no plano-template e decomposição/provas no tasks-template/guia |
| SC-001 a SC-004 | T008 | links locais, estrutura, diff e scanner versionado |

Validação local proporcional: links Markdown, referências de gate/checkpoints, cinco seções no template, campos de aceite e IDs preservados, revisão de diff, `git diff --cached --check` e `./scripts/security/check-versioned-secrets.ps1` após stage. Testes de aplicação/lint/typecheck/build/Docker local são N/A; CI do PR continua obrigatório e não dispensado.

## Constitution Check final — evidência anterior ao PR

| Princípio | Evidência efetiva | Resultado | Pendência / responsável |
| --- | --- | --- | --- |
| CON-01 | Staged diff contém somente quinze arquivos Markdown previstos | PASS | Nenhuma alteração de runtime |
| CON-02 | Diff sem novas camadas/dependências; contexto técnico e estrutura exigem fonte/motivo | PASS | Testes de arquitetura N/A: sem código |
| CON-03 | Diff não altera auth/tenant; template preserva atores/tenant/negativos por regra | PASS | Testes de autorização N/A: sem contrato/código |
| CON-04 | Roteiro separa fato/hipótese/decisão e bloqueia H3; nenhuma regra funcional criada | PASS | H2 documental pendente, não libera integração |
| CON-05 | Scanner versionado aprovado após stage; sem dados pessoais/segredos indevidos no diff | PASS | Migrations N/A: schema inalterado |
| CON-06 | Lista de arquivos e diff sem REST/BFF/eventos; contratos ficam no plano | PASS | OpenAPI N/A: endpoints inalterados |
| CON-07 | Exemplo explicitamente fictício/incompleto, sem claim real sobre Escala | PASS | Nenhuma UI alterada |
| CON-08 | Links de quatorze arquivos resolvidos, cinco seções/gates/H1-H3 verificados, diff sem erro; proveniência real no intake | PASS local | CI novo PR obrigatório, pendente |
| CON-09 | Base 91484273, branch isolada e ledger do merge atualizado; registro de commit/PR e revisão H2 separados | PASS local | Wemerson: revisar MVP; CI/integração pendentes |

Verificações realizadas em 2026-10-08 por Codex; não constituem revisão humana independente. SHA do registro final constará no PR para evitar auto-referência impossível.

## Riscos e rollback

Risco: confundir prompt com hook automático, fontes prévias com entrevista fictícia ou fim do dossiê com autorização. Mitigar por estados reais, registro de origem, gate e exemplos rotulados. Retomada deve revalidar escopo sem repetir perguntas já respondidas.

Rollback: reverter os commits da issue #122 via PR, incluindo links e templates; manter o merge #121 e seu histórico. Preservar dados/runtime e trabalho local de UI. Specs futuras que passem a depender do roteiro precisam de avaliação antes de removê-lo.
