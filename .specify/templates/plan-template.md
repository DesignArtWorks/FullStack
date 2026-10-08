# Plano — <nome>

Spec: `<numero>-<slug>` | Issue: #<numero> | Branch: `<tipo>/issue-<numero>-<slug>`
Base remota / SHA: <real> | Constituição / versão: <referência> | Data: <data>

## Análise, impacto e arquivos previstos

<Código/testes existentes, fontes, consumidores, dívida, versões do commit analisado e arquivos antes de implementar. Distinguir análise estática de execução validada.>

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
