# Prompts de trabalho do Escala

Copiar o prompt apropriado, substituindo os campos entre colchetes. Eles orientam a tarefa; não concedem permissão para ações externas adicionais. Ler [contexto](contexto-escala.md) e AGENTS.md. Skills ficam em .agents/skills; sua disponibilidade automática depende da descoberta do cliente, e o caminho pode ser indicado explicitamente.

## Retomar / organizar

Leia docs/ai/contexto-escala.md e as instruções aplicáveis. Meu objetivo é [resultado]. Confira Git e GitHub; preserve alterações locais. Identifique evidências, pendências, dependências e um próximo passo executável. Atualize o contexto apenas se houver decisão ou evidência nova; não transforme hipótese em capacidade entregue.

## Implementar uma issue

Use $escala-issue-delivery em .agents/skills/escala-issue-delivery/SKILL.md para implementar a issue [número]. Primeiro execute docs/ai/entrevista-socratica.md: uma pergunta por mensagem, dossiê com proveniência e gate do recorte, reutilizando respostas humanas válidas com origem. Só depois da entrevista validada construa/atualize spec, plan e tasks. Analise problema, ator, contratos, tenant, LGPD, arquivos e testes. Parta do develop remoto atualizado e crie branch no formato do AGENTS. Execute por fases, com revisão humana antes de alterar código existente e após o MVP; decisões fora da spec retornam ao diálogo. [Fase autorizada / ponto de parada: indicar]. [Publicação/merge autorizados: indicar o escopo]. Não contorne proteção/checks. Entregue PR, evidências, riscos e rollback.

## Iniciar entrevista socrática

Leia o prompt pronto em [entrevista-socratica.md](entrevista-socratica.md) e aplique-o à issue [número] e ao pedido [texto]. Durante a entrevista, faça somente uma pergunta por mensagem, sem solução técnica. Registre respostas reais e fontes em specs/[numero-slug]/interview.md; não marque validado sem evidência humana.

## Executar somente uma fase

Leia AGENTS, interview.md, spec.md, plan.md e tasks.md de specs/[numero-slug]. Confira o gate socrático e as aprovações registradas. Execute somente [fase/IDs], valide o checkpoint com os comandos aplicáveis, mostre diff/resultado/riscos e pare para revisão humana. Não execute a próxima fase nem integre PR sem autorização correspondente. Se houver decisão ausente da spec, pare o trecho dependente e volte à entrevista. Comandos do Spec Kit são exemplos de conversa, não comandos instalados por esta documentação.

## Designer / UX

Use $escala-product-design em .agents/skills/escala-product-design/SKILL.md para [fluxo/tela]. Leia docs/ai/designer-escala.md, revise o código e referência visual disponível. Produza fluxo e handoff com estados, responsividade, acessibilidade, tokens, copy e permissões. Separe proposta de aprovação e inspeção estrutural de teste visual. [Somente especificar ou também implementar: indicar].

## Prontidão AWS

Use $escala-aws-readiness em .agents/skills/escala-aws-readiness/SKILL.md para avaliar [ambiente/release]. Revise ADRs 005/006/007, infraestrutura e evidências de CI. Entregue inventário, gaps, orçamento com fonte/data, plano de homologação, restore, carga, rollback e decisão de gate. Esta solicitação é avaliação; provisionamento só se explicitamente autorizado.

## Marketing

Use $escala-go-to-market em .agents/skills/escala-go-to-market/SKILL.md para preparar [landing/campanha/piloto] para [nicho]. Defina hipótese, público, mensagem, capacidades comprovadas, CTA, consentimento, canal e métrica com denominador/janela. Não invente cliente, ganho ou compliance. [Somente rascunho ou publicar em destino especificado: indicar].

## Revisão / QA

Revise [diff/PR] contra critérios da issue e AppSec. Priorize regressão, autorização negativa, tenant, concorrência e contrato. Diferencie defeito comprovado de risco. Execute checks proporcionais e registre comandos/resultados. Não declare todos os testes aprovados se houve skip ou dependência simulada.

## Comunicação no Slack

Quando eu autorizar o envio, confira as issues e PRs em [escopo/período], leia o canal existente e publique em [canal]. Inclua mudança, evidência, status de integração/produção, riscos e próximos passos. Não use atualização como data de fechamento nem repita anúncio idêntico.
