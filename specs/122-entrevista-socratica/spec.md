# Especificação — Entrevista e execução por fases

Spec: `122-entrevista-socratica` | Issue: [#122](https://github.com/DesignArtWorks/FullStack/issues/122)
Data: 2026-10-08 | Estado: MVP documental em preparação para revisão humana
Origem: [intake.md](intake.md). Esta adoção não representa entrevista retrospectiva realizada.

## Problema, atores e valor

Pedidos vagos podem virar regras implícitas no código. Wemerson quer que Codex exponha essas decisões antes de implementar, registre o entendimento e execute por fases sob revisão humana. A documentação deve preservar o protocolo fornecido e as decisões já registradas, sem propor soluções durante a entrevista ou inventar aprovação.

## US1 (P1) — Preparar e usar entrevista antes de implementar

Como responsável do Escala, quero um roteiro versionado e dossiê ligado à mudança para esclarecer público, comportamento e sucesso antes da execução.

- Dado pedido com adjetivo vago, quando a entrevista continuar, então a próxima pergunta exige quantificação observável sem sugerir um número.
- Dadas quatro respostas humanas, quando o agente continuar, então faz síntese curta de confirmado/suposto/conflito e uma única pergunta.
- Dado encerramento com decisões essenciais ausentes, quando o dossiê for produzido, então fica INCOMPLETO e o trecho dependente não é implementado.
- Dado entendimento confirmado, quando a implementação for preparada, então tarefas/commits preservam origem e checkpoints humanos.
- Dado pedido limitado a uma fase, quando o checkpoint for demonstrado, então o agente para para revisão e não executa a fase seguinte por inferência.

## Requisitos e origem

| ID | Requisito | Origem |
| --- | --- | --- |
| FR-001 | O processo deve executar o roteiro antes de qualquer implementação e revisar sua validade em retomadas. | PROV-002; CON-04/08 |
| FR-002 | O entrevistador deve respeitar uma pergunta por mensagem, cinco fases, seis categorias, quantificação e síntese periódica. | PROV-003 |
| FR-003 | O processo deve produzir dossiê de cinco seções com fontes, classificação e abertas com dono. | PROV-003; CON-08 |
| FR-004 | O processo deve ligar entrevista/decisão a requisito, plano, tarefa, verificação, commit e PR. | PROV-002/003; CON-08/09 |
| FR-005 | O processo deve executar por fases com revisão antes de alterar código existente e após MVP; decisões fora da spec bloqueiam o trecho dependente. | PROV-004; CON-04/09 |
| FR-006 | O processo deve fornecer exemplo didático sem atribuí-lo a dados reais do Escala. | Material fornecido; CON-07/08 |
| FR-007 | O registro anterior deve refletir o merge concluído em develop sem afirmar promoção main. | PROV-001/005/006; CON-09 |
| FR-008 | O plano deve declarar contexto técnico/estrutura e apoios pertinentes; tarefas devem calibrar decomposição e distinguir falha de regra de falha de ambiente. | PROV-008; CON-02/08 |

## Bordas e invariantes

Opinião sem fonte permanece pressuposto; fonte de uma fala não prova seu conteúdo. Fechar entrevista cedo produz INCOMPLETA. Dossiê sem respostas reais não pode ser VALIDADO. Silêncio/timeout/teste verde/commit não é aprovação. Aprovação humana anterior conserva validade no escopo concreto. H1 é N/A quando nenhum código existente será alterado; H2 continua pendente até revisão. Pergunta aberta sem dono conhecido deve registrar responsável a designar.

## Critérios de sucesso

- SC-001: roteiro contém o contrato, os cinco critérios de fase e as seis categorias; template tem exatamente cinco seções de dossiê.
- SC-002: AGENTS, guia e templates referenciam entrevista/gate, e tasks/plan registram H1/H2/H3.
- SC-003: cada tarefa indica regra/FR/US, dependência, arquivo e verificação, com resultado real separado do esperado.
- SC-004: somente documentos são alterados; nenhuma CLI/automação instalada, nenhuma entrevista/aprovação simulada e nenhuma promoção main realizada.
- SC-005: template do plano contém Technical Context/Project Structure e decisões sobre apoios; template de tarefas distingue verificação atômica, checkpoint do agente e parada humana.

## Escopo e restrições

Não altera runtime, contrato REST, schema, permissão ou princípios constitucionais. Não cria plugin/skill ou serviço. O gate é operacional/documental via instruções do agente e revisão, não enforcement automático do Git. A fase de adoção entrega um MVP documental completo para aprovação humana antes de integrar a nova política.
