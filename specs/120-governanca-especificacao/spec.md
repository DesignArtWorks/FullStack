# Especificação — Governança por especificação

Spec: `120-governanca-especificacao` | Issue: [#120](https://github.com/DesignArtWorks/FullStack/issues/120)
Data: 2026-10-08 | Estado: documentação produzida; revisão e entrega pendentes

## Problema, atores e valor

Decisões do Escala estão distribuídas entre instruções, arquitetura, ADRs e backlog. Desenvolvedores, agentes e revisores precisam de princípios verificáveis e um caminho de execução que preserve essas decisões e permita acompanhar tarefas até a entrega. O pedido do usuário e as decisões locais são a origem desta mudança; exemplos anexados são referência de método, não novas regras do produto.

## Histórias de usuário

### US1 — Preservar decisões (P1)

Como responsável de arquitetura, quero uma constituição para que implementações não alterem fronteiras e garantias já definidas.

- Dado um plano de mudança, quando confrontado com a constituição, então cada princípio tem desenho/evidência e resultado explícitos.
- Dada uma divergência, quando o implementador encontra uma violação, então corrige o desenho ou registra proposta de emenda aprovada separadamente; não aceita exceção sozinho.

### US2 — Executar com rastreabilidade (P1)

Como desenvolvedor, quero um plano e tarefas com dependências e critérios de conclusão para acompanhar trabalho, validações e commits sem perder a regra de origem.

- Dada uma tarefa, quando implementada, então a matriz identifica requisito, regra, artefato, verificação e commit real.
- Dado um teste não executado ou gate falho, quando o status é atualizado, então a pendência permanece visível e a entrega não é marcada concluída.

### US3 — Revisar e entregar (P2)

Como revisor, quero um registro que permita localizar escopo, comandos, resultados, riscos e rollback antes de integrar por PR.

- Dada uma mudança validada, quando o PR é aberto, então cita issue/spec/tarefas e informa gates do head revisado.
- Dado um PR aberto, quando checks ou promoção não terminaram, então issue e tarefas de entrega permanecem abertas.

## Requisitos funcionais e origem

| ID | Requisito | Origem / princípios |
| --- | --- | --- |
| FR-001 | O processo deve consolidar princípios obrigatórios e política de emenda, sem congelar versões técnicas. | Pedido do usuário; AGENTS; CON-01 a CON-09 |
| FR-002 | O processo deve registrar o estado observado, fontes, divergências e limites da análise. | Pedido: analisar projeto; CON-02, CON-08 |
| FR-003 | O processo deve fornecer plano com Constitution Check, impacto, arquivos e validações aplicáveis. | Pedido: como construir; CON-01 a CON-09 |
| FR-004 | O processo deve fornecer sequência de tarefas com IDs, dependências, aceite e evidência. | Pedido: sequência de tarefas; CON-08, CON-09 |
| FR-005 | O processo deve ligar regra/requisito/tarefa/verificação/artefato/commit/PR e distinguir pendente de concluído. | Pedido: controles de tarefas e commits; CON-08, CON-09 |
| FR-006 | O processo deve oferecer modelos reutilizáveis e instruir os agentes a consultá-los. | Derivação do uso recorrente solicitado; CON-08 |
| FR-007 | O processo deve preservar trabalho local e exigir entrega por develop e main sob os gates vigentes. | AGENTS, workflow; CON-09 |

## Entidades conceituais

Princípio (obrigação estável); regra (comportamento com origem); especificação (o quê/por quê); plano (como e gates); tarefa (passo verificável); evidência (resultado real); commit (snapshot); PR (revisão/integração). Uma tarefa pode atender vários requisitos; um commit pode agrupar tarefas coesas.

## Casos de borda

- Checkout sujo: isolar a mudança e preservar alterações existentes.
- Regra sem ID: atribuir alias estável com fonte, sem inventar comportamento.
- IDs iguais em specs diferentes: usar `<spec>:<ID>` nos registros.
- Teste ausente/skipped, check falho ou revisão pendente: status pendente/falho, nunca sucesso.
- Regra de negócio contraditória: responsável decide antes da implementação dependente.
- Migração arquitetural parcial: documentar dívida, sem exigir reescrita geral nem afirmar conclusão.

## Critérios de sucesso

- SC-001: FR-001 a FR-007 possuem artefato e verificação identificáveis na matriz do plano.
- SC-002: todos os nove princípios têm Constitution Check inicial e revisão anterior ao PR.
- SC-003: cada tarefa tem ID, dependência, critério e registro; tarefas externas não executadas ficam abertas.
- SC-004: nenhuma alteração de runtime, contrato REST, banco, dependência ou trabalho local de UI faz parte desta mudança.

## Escopo e suposições

Mudança documental. Não implementa o backlog funcional nem instala CLI/skills do Spec Kit. Não altera contratos ou envia comunicações operacionais. Não adiciona automação de CI para os novos documentos: a primeira versão usa revisão e verificação documental. A aplicação automática pode ser uma issue futura.

A numeração da pasta usa a issue #120; não depende de autoincremento da CLI. A promoção para main segue a governança existente, mas não deve ser confundida com autorização para promover outras alterações acumuladas em develop sem revisão.
