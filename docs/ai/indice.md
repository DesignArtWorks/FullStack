# Materiais de trabalho com IA

Entrada: [contexto](contexto-escala.md). Uso diário: [prompts](prompts-escala.md). UX/UI: [designer](designer-escala.md).

Antes de implementar: [entrevista socrática](entrevista-socratica.md), [exemplo didático](exemplo-intervencao-socratica.md) e [template do dossiê](../../.specify/templates/interview-template.md). Entrevista → proposta validada → spec → plan → tasks → implementação por fases, com revisão humana antes de alterar código existente e após o MVP.

Skills versionadas no repositório:
- [Entrega de issues](../../.agents/skills/escala-issue-delivery/SKILL.md)
- [Produto e design](../../.agents/skills/escala-product-design/SKILL.md)
- [Prontidão AWS](../../.agents/skills/escala-aws-readiness/SKILL.md)
- [Go-to-market](../../.agents/skills/escala-go-to-market/SKILL.md)

Escopo da issue #116: documentação e instruções, sem runtime, dependências ou configurações globais. Cada skill possui gatilho próprio e carrega somente as referências da tarefa. Se não aparecer no catálogo da sessão atual, solicitar leitura do caminho; não presumir instalação global.

## Análise que orientou a atualização

AGENTS.md já concentra arquitetura, segurança e governança. Roadmap/OKRs e ADRs são fontes existentes; duplicá-los integralmente aumenta divergência. Criamos um índice curto, contextos por atividade e prompts que apontam para as fontes.

O design local e mudanças de frontend ainda não versionados precisam de reconciliação independente. O guia registra a limitação e evita tratar direção proposta como design aprovado. Versões de dependências são lidas de manifests; o contexto registra a divergência observada sem reescrever o histórico.

A equipe continua Wemerson + Codex. Os papéis das skills não criam colaboradores humanos, subagentes, automações, permissões produtivas ou serviços no runtime.

## Manutenção

Atualizar material quando decisão, contrato ou evidência muda. Estado transitório pertence à issue/PR/handoff, com data e SHA. Revisar referências após renomear arquivos; validar SKILL.md com o quick_validate da skill-creator disponível no ambiente. A validação estrutural não prova qualidade do comportamento.

Rollback: revert do commit documental. Merge via PR em develop; promoção de main depende dos gates de release e não faz parte desta solicitação.
