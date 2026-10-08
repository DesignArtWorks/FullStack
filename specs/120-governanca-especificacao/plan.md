# Plano — Constituição e execução rastreável

Spec: `120-governanca-especificacao` | Issue: #120 | Data: 2026-10-08
Branch: `docs/issue-120-constitution-plan`
Base: `origin/develop` em `08d0dcdb8646804b9a8913b5ea94d36dc3199890`
Constituição: [1.0.0](../../.specify/memory/constitution.md)
Entradas: [spec.md](spec.md), [analysis.md](analysis.md) | Execução: [tasks.md](tasks.md)

## Desenho e escopo

Consolidar os princípios sem alterar runtime; separar comportamento (spec), desenho (plan) e execução (tasks). A constituição é única por projeto, enquanto cada mudança terá `specs/<numero>-<slug>/`. A presente pasta documenta a adoção do processo, não a implementação de todas as features do roadmap.

Não instalar o Spec Kit nem executar `specify init` sobre o repositório: isso poderia gerar/sobrescrever instruções e scripts fora do escopo. Adotar layout e templates locais agora; integração da CLI, verificador automático e gates adicionais podem ter issues próprias. Não há novas dependências ou alteração REST/eventos nesta entrega.

## Constitution Check — antes das tarefas

| Princípio | Desenho / evidência | Resultado |
| --- | --- | --- |
| CON-01 | Documentos confirmam diretórios oficiais, BFF/backend, CMS editorial e banco operacional. Nenhum runtime alterado. | PASS |
| CON-02 | Análise distingue domínio puro, serviços Spring e migração parcial; plano reutilizável define portas/adapters. | PASS |
| CON-03 | Constituição/templates exigem identidade atual, tenant confiável e testes negativos. Esta mudança não acessa dados privados. | PASS |
| CON-04 | Regras derivam de fontes oficiais; capacidades futuras e decisões ausentes ficam explícitas. Nenhuma regra operacional alterada. | PASS |
| CON-05 | Sem migrations ou dados de usuário; regras de Flyway, auditoria e minimização preservadas. | PASS |
| CON-06 | Contratos permanecem iguais; templates incluem contrato/comunicação e atualização OpenAPI quando aplicável. | PASS |
| CON-07 | Sem alteração de UI; plano inclui estados UX/revisão apropriada em futuras features. | PASS |
| CON-08 | FR/SC, tarefas e artefatos definidos; verificações documentais e registro de commits. | PASS no desenho; evidência de execução em tasks |
| CON-09 | Fetch realizado e worktree dedicado criado; commit/push/PR/checks/promoção serão registrados sem antecipar resultado. | PASS no desenho; entrega PENDING |

Nenhuma exceção constitucional proposta. PASS do desenho não comprova tests/builds ou entrega. Revisão anterior ao PR deve reconferir todos os princípios e o diff; status dos gates externos permanece no ledger.

## Arquivos previstos e impacto

| Arquivo | Finalidade |
| --- | --- |
| `.specify/memory/constitution.md` | Princípios, evidências e política de emenda |
| `.specify/templates/spec-template.md` | Histórias, regras/origem e critérios de sucesso |
| `.specify/templates/plan-template.md` | Desenho, Constitution Check, contratos e validações |
| `.specify/templates/tasks-template.md` | Sequência, aceite, dependências e ledger Git |
| `specs/120-governanca-especificacao/spec.md` | Comportamento esperado desta adoção |
| `specs/120-governanca-especificacao/analysis.md` | Inventário, divergências e limites |
| `specs/120-governanca-especificacao/plan.md` | Desenho e controles desta mudança |
| `specs/120-governanca-especificacao/tasks.md` | Controle de execução e entrega |
| `docs/desenvolvimento-orientado-especificacao.md` | Guia prático para próximas issues |
| `AGENTS.md` | Ponte para constituição e fluxo de artefatos |

Sem impacto no schema, autenticação, contratos, UI, dependências ou permissões. Risco documental: interpretação como migração já concluída, automação instalada ou liberação de gates; mitigação com estados/evidências explícitos e revisão do PR.

## Como construir futuras features respeitando a arquitetura

1. **Analisar o recorte:** ler fontes oficiais, caso de uso existente, consumidores, testes e estado Git. Identificar contexto proprietário e dívida tocada. Registrar arquivos antes da implementação.
2. **Especificar:** definir problema/ator/valor, histórias Given/When/Then, IDs de regra com origem, FR e SC mensuráveis sem tecnologia. Limites/direitos contraditórios exigem decisão do responsável; a tarefa dependente fica bloqueada.
3. **Planejar domínio e aplicação:** models/value objects/policies puros; portas de entrada/saída; caso de uso orquestra domínio. Reaproveitar `core/<contexto>` onde adequado; não reorganizar todos os pacotes por conveniência.
4. **Planejar adapters e wiring:** controller/DTO de transporte → caso de uso → porta → adapter JPA/integração. Resolver identidade/tenant no boundary confiável, passando escopo explicitamente às portas. Spring instancia os casos de uso em configuração, sem levar JPA para domain.
5. **Planejar dados e contrato:** constraints/índices tenant-aware, Flyway, soft delete, append-only, compatibilidade, backfill e recuperação; schemas REST/eventos, autorização, idempotência, retry e OpenAPI. Considerar conflito no commit, não só aquisição do lock.
6. **Planejar frontend:** Route Handler BFF explícito → adapter/hook/feature → UI; Server Page compõe dados e componentes. Token fica server-side; regra final backend. Estado local de editor é prévia, não autorização nem publicação.
7. **Decompor tarefas:** teste de regra/negativos, domínio, portas/caso de uso, adapter/migration, REST/BFF, UI, contrato/auditoria, validação e PR. Declarar dependências; só marcar paralelismo quando não houver conflito de arquivos/decisão.
8. **Implementar e validar:** executar as tarefas aprovadas. Descoberta que muda regra ou arquitetura retorna a spec/plan; não inventar decisão no implement. Vincular IDs de origem aos testes e ponto de código relevante.
9. **Revisar e entregar:** Constitution Check final, diff, evidência real, commit coeso, push e PR para develop; após gates/aprovações e merge, promoção de develop para main pelo processo oficial.

## Rastreabilidade desta adoção

IDs abaixo são locais à spec `120-governanca-especificacao`; CON é global.

| Requisito / sucesso | Princípios | Tarefas | Artefatos / verificação |
| --- | --- | --- | --- |
| FR-001 / SC-002 | CON-01 a CON-09 | T003, T008 | constitution e Constitution Check; revisão normativa |
| FR-002 / SC-004 | CON-02, CON-08 | T001, T002 | analysis; comparação com código/manifests/workflow |
| FR-003 / SC-001 | Todos | T004 | plan; tabela de impacto/gates |
| FR-004 / SC-003 | CON-08, CON-09 | T005 | tasks; critérios/dependências/ledger |
| FR-005 / SC-001, SC-003 | CON-08, CON-09 | T005, T009, T010, T011 | matriz, commits e PR; SHAs/URLs reais |
| FR-006 / SC-001 | CON-08 | T006, T007 | templates, guia e AGENTS; revisão de uso |
| FR-007 / SC-004 | CON-09 | T001, T009 a T012 | branch isolada, diff e entrega protegida |

## Validação e evidência

Para esta mudança documental: verificar links locais Markdown, referências/IDs, consistência dos nove princípios, diff sem runtime/segredos e `git diff --check`. O scanner versionado `scripts/security/check-versioned-secrets.ps1` deve ser executado quando os arquivos estiverem tracked. Não criar testes de aplicação que apenas verificam texto.

Lint/typecheck/build/testes de runtime e Docker local são N/A para o escopo documental, com justificativa no registro. Isso não dispensa o CI do PR: o workflow atual não tem filtros por caminho e exige todos os jobs.

| Gate do CI vigente | Expectativa |
| --- | --- |
| Security / Versioned Secret Scan | Nenhuma credencial versionada |
| Backend / Unit and Build | `mvn -B package` na imagem Maven/Java 25 |
| Backend / Integration | `mvn -B -Pintegration test`, PostgreSQL/Redis/Flyway reais e sem skips |
| Frontend / Quality and Build | install frozen, lint, typecheck e build |
| CMS / Build | `npm ci` e build |
| Security / Dependency SCA | Scan e política de supressões vigentes |
| Frontend / Security E2E | Suite Playwright do workflow efetivo |
| CI / Required Gate | Todos os jobs exigidos em sucesso no head candidato |

Para mudança backend futura, a partir da raiz e ambiente configurado: iniciar/rebuild backend via Docker Compose; verificar HTTP/sucesso de `/actuator/health`, `/swagger-ui/index.html` e JSON `/v3/api-docs`, além dos endpoints afetados. Usar portas efetivas do ambiente; Swagger desabilitado em produção é política distinta, não falha de documentação local. Não usar stack compartilhada de outra tarefa como evidência do novo código.

## Commits, PR e promoção

- Commit 1: constituição, análise, spec e plano (T002–T004).
- Commit 2: templates, tasks, guia e ponte AGENTS (T005–T008); corpo registra issue/spec/tasks/verificações.
- Registrar SHAs reais em tasks no commit subsequente ou no PR; não tentar embutir o hash de um commit dentro dele mesmo.
- Fazer push da branch dedicada e abrir PR para develop, citando `Refs #120`. Não usar fechamento automático antecipado quando main ainda não recebeu a promoção.
- Antes de merge: atualizar base se necessário, resolver conflitos na branch, repetir checks invalidados, verificar head e revisão. Sem bypass.
- Após merge develop, revisar diff `main...develop`, abrir PR de promoção para main e aguardar gates/aprovação. Não promover conteúdo acumulado sem análise. Registrar merges e pushes efetivos antes de fechar a issue com inventário final.

## Rollback

Reverter os commits desta issue via PR dedicado; remover a ponte AGENTS e os artefatos introduzidos no mesmo revert. Sem migration ou restauração de dados. Specs futuras que já dependam desta constituição precisam de revisão antes do revert. Não desfazer alterações de UI presentes no checkout principal.
