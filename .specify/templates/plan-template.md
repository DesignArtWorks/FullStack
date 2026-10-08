# Plano — <nome>

Spec: `<numero>-<slug>` | Issue: #<numero> | Branch: `<tipo>/issue-<numero>-<slug>`
Base remota / SHA: <real> | Constituição / versão: <referência> | Data: <data>

## Gate socrático e modo de execução

Dossiê: `interview.md` da pasta da spec. Última revisão do pedido: <data/origem>. Gate: PENDING.
<Canônicas/decisões confirmadas, PROV-IDs ligados a FR/RN, abertas/conflitos e recorte bloqueado; não fabricar entrevista.>

Modo padrão: **por fases**, conforme decisão de Wemerson em 2026-10-08. Definir Setup → Foundational → US1/MVP → demais histórias → Polish/entrega. Descrever cada checkpoint e comando que comprova sua conclusão.

| Parada humana | Artefato concreto a revisar | Evidência / aprovação / escopo liberado |
| --- | --- | --- |
| H1 — antes de alterar código existente | <spec/plan, arquivos, contratos, riscos e diff planejado> | PENDING; <responsável> |
| H2 — após primeira fatia completa/MVP | <resultado demonstrável, diff e verificações reais> | PENDING; <responsável> |
| H3 — decisão fora da spec | <lacuna, PROV-ID e tarefa dependente> | Bloqueia trecho até decisão; <responsável> |

H1 pode ser N/A somente se nenhum código existente for alterado, com motivo. H2 não é dispensado por testes verdes. Aprovação anterior vale se seu escopo concreto está registrado. Documentos de adoção da política não comprovam entrevista retrospectiva. Commit/push/PR para revisão não significam aprovação da fase seguinte ou autorização de merge.

## Technical Context — contexto técnico do recorte

| Campo | Estado observado / escolha justificada / fonte |
| --- | --- |
| Linguagem e versão | <manifests/lockfiles e commit analisado> |
| Dependências | <existentes reutilizadas; novidade exige justificativa> |
| Ferramentas de teste/verificação | <suite/comandos reais pertinentes> |
| Plataforma e ambientes | <Docker/Next/BFF/Spring/profiles ou N/A> |
| Dados e persistência | <PostgreSQL/Flyway/Redis conforme necessidade ou N/A> |
| Restrições | <constituição, autorização, compatibilidade, prazo e dados> |
| Escala/performance | <requisito aprovado e origem; nunca inventar meta> |

Tecnologia aparece no plano; a spec descreve comportamento/valor. Uma escolha técnica que muda resultado, direito, limite ou critério de sucesso deve voltar ao dono da regra e à spec, não ficar escondida como implementação.

## Project Structure — análise, impacto e arquivos previstos

<Código/testes existentes, fontes, consumidores, dívida, versões do commit analisado e arquivos antes de implementar. Distinguir análise estática de execução validada.>

| Caminho | Criar/alterar/reusar | Motivo / FR/RN | Impacto em consumidores/testes |
| --- | --- | --- | --- |
| <arquivo real> | <ação> | <origem e decisão> | <impacto> |

## Documentos de apoio, conforme necessidade

| Artefato | Quando usar / conteúdo | Decisão nesta mudança |
| --- | --- | --- |
| research.md | Escolha técnica incerta; alternativas e decisão com fonte | <usar ou N/A justificado> |
| data-model.md | Dados novos/alterados; entidades, tenant, constraints/migration | <usar ou N/A justificado> |
| contracts/ | REST/BFF/eventos alterados; schema/erros/compatibilidade | <usar ou N/A justificado> |
| quickstart.md | Verificação manual de feature; ambiente, comandos e resultado esperado | <usar ou cobrir no plano/N/A> |

Não gerar arquivos vazios para cumprir ritual. Apoio complementa, não substitui Constitution Check, lista de arquivos e decisões humanas.

## Constitution Check

| Princípio | Aplicabilidade e desenho/evidência | Resultado | Pendência / responsável |
| --- | --- | --- | --- |
| CON-01 | <fronteiras/fontes> | PENDING | <ação> |
| CON-02 | <domínio/portas/adapters> | PENDING | <ação> |
| CON-03 | <identidade/tenant/autorização> | PENDING | <ação> |
| CON-04 | <invariantes/transições/IA> | PENDING | <ação> |
| CON-05 | <dados/auditoria/Flyway/LGPD> | PENDING | <ação> |
| CON-06 | <contratos/comunicações/health> | PENDING | <ação> |
| CON-07 | <UX/acessibilidade/claims> | PENDING | <ação> |
| CON-08 | <IDs/testes/gates> | PENDING | <ação> |
| CON-09 | <branch/commits/PR/checks> | PENDING | <ação> |

Resultados: PASS, FAIL, PENDING ou N/A justificado. Repetir antes do PR. Não autorizar violação numa tabela de exceções; corrigir desenho ou propor emenda separada.

## Desenho hexagonal e frontend

<Contexto proprietário; domínio puro; caso de uso; portas; adapters web/JPA/integração; wiring. Tenant explícito nas portas. Migração incremental. BFF explícito, adapter/hook/feature, página e UI; token server-side.>

## Contratos e comunicações

<Ou N/A justificado. Produtor/consumidor, rota/evento/versão, schemas/nulabilidade, autenticação, autorização, tenant, sucesso/erros, compatibilidade, idempotência, limites e observabilidade; OpenAPI. Para comunicação: gatilho, destinatário, canal, payload mínimo, locale/CTA, retry/duplicidade, indisponibilidade e auditoria.>

## Dados e operação

<Ou N/A justificado. Entidades tenant-bound/globais/pré-tenant; constraints/índices; Flyway/backfill; soft delete/append-only; transações, versão/locks; segredos, LGPD, dependências/feature flags, rollout/rollback.>

## UX e validação

<Loading/empty/error/success/conflict/forbidden, teclado, responsividade e fonte visual aprovada conforme impacto.>

| Cenário / requisito / regra | Verificação existente ou nova | Comando / ambiente | Aceite |
| --- | --- | --- | --- |
| <FR/RN> | <teste relevante> | <comando> | <resultado esperado> |

Incluir negativos/cross-tenant para recursos tenant-bound e testes reais para banco/locks. Unitários com mocks não provam integração. Backend alterado exige Docker + health/Swagger/OpenAPI. Conferir jobs efetivos do workflow; falha/skip não é PASS. Documento pode justificar N/A local, mas não dispensa CI obrigatório.

## Rastreabilidade, tarefas e Git

| Regra | FR / US / SC | Tarefas | Teste/verificação | Código/artefato |
| --- | --- | --- | --- | --- |
| <ID> | <IDs> | <TIDs> | <caminho/símbolo> | <caminho/símbolo> |

<Commits coesos, issue/spec/IDs no corpo; ledger com SHAs reais. Branch → PR develop → promoção develop/main sob proteção. Checks/revisões/rollback e pendências antes de encerrar issue.>

## Constitution Check final — antes do PR

Head revisado / data: <SHA real / data>; responsável: <nome>. Preencher novamente com evidência do resultado, sem copiar automaticamente o check de desenho.

| Princípio | Evidência do diff / validação | Resultado | Pendência / responsável |
| --- | --- | --- | --- |
| CON-01 | <fronteiras preservadas> | PENDING | <ação> |
| CON-02 | <dependências e limites efetivos> | PENDING | <ação> |
| CON-03 | <autorização/tenant e negativos> | PENDING | <ação> |
| CON-04 | <invariantes/transições testadas> | PENDING | <ação> |
| CON-05 | <dados/auditoria/migrations> | PENDING | <ação> |
| CON-06 | <contratos/comunicações/OpenAPI> | PENDING | <ação> |
| CON-07 | <UX/acessibilidade/claims> | PENDING | <ação> |
| CON-08 | <matriz, testes/verificações e gates> | PENDING | <ação> |
| CON-09 | <base, commits/push, escopo e controles PR> | PENDING | <ação> |

N/A exige motivo explícito por princípio. Separar revisão local, CI e merge; pendência externa não pode virar PASS por ausência de execução.
