# Controles do piloto e respostas à revisão de governança

Data: 2026-10-08 | Base analisada: 0efcc18f0dc3a8cc53974de363d3f6128ea73cac | Estado: análise/proposta para H1
Fontes: [constituição](../../.specify/memory/constitution.md), [dossiê](interview.md), [plano](plan.md), [contexto da equipe](../../docs/ai/contexto-escala.md).

| Pergunta original | Resposta com evidência | Ação proporcional proposta |
| --- | --- | --- |
| Como garantir o fluxo? | AGENTS/templates/skill orientam o agente; workflow exige gates técnicos. Não há script que prove entrevista/H1/H2/H3 ou rastreabilidade | Usar ledger e revisão neste piloto; especificar futura automação de estrutura sem alegar que valida intenção/aprovação |
| Arquitetura atual ou meta? | Domínio de scheduling possui modelos/policies e testes puros; core/scheduling/application/GenerateScheduleService depende de DTO e entidade Employee. Não há comprovação de migração completa | Preservar runtime; registrar dívida por limite tocado. Testes automáticos de dependências são proposta futura, não instalados pela #106 |
| Quem aprova regras/emendas? | Contexto identifica Wemerson como fundador/PO/desenvolvedor; constituição exige emenda em PR separado com responsáveis conforme impacto. CODEOWNERS aponta wemersonnino para workflow/scripts/allowlists | Wemerson revisa H1/H2 e exceções; Codex fornece provas, não aprovação humana independente |
| Como sincronizar tarefas e Git? | Templates exigem IDs/resultados/SHAs; SHA de merge só existe após integração | Tasks registra estado até seu último commit; PR/issue registra estado final/merge e evidência humana; próxima atualização pode conciliar o snapshot, sem loop de commits fictícios |
| Qual piloto e critério? | Quadro Ready: #103/#106/#109; #99 In progress agrega entregas, não é autorização para implementá-las juntas | #106 é a menor fatia reproduzível localmente sem depender de infraestrutura externa; medir comportamento e trilha, não inventar meta de produtividade |

## Matriz de controles

| Controle | Existe | Humano | Automação necessária/proposta | Limite da evidência |
| --- | --- | --- | --- | --- |
| Backend unit/build e integração PostgreSQL/Redis/Flyway | Workflow efetivo | Revisa resultados | Preservar | CI do PR atual ainda não executado |
| Frontend lint/typecheck/build e Security E2E | Workflow efetivo | Revisa fluxos afetados | Preservar | E2E usa backend simulado, não substitui integração Spring |
| CMS build/testes de segurança | Workflow efetivo | Revisa fronteira editorial | Preservar | Não prova todo CMS seguro |
| SCA e Required Gate | Workflow efetivo | Revisa exceções/risco | Preservar | Não dispensar por diff documental |
| Segredos em arquivos rastreados | PowerShell, quatro regex | Revisa finding | Reutilizar como complemento | Não cobre história completa nem todos os padrões |
| Histórico e fixtures negativas | Artefatos Gitleaks existentes | Revisa allowlist e desenho | Conectar e testar no CI nesta US1 | Documento antigo afirma automação que o job atual não executa |
| Autenticação de credenciais locais | Não comprovada nesta análise | Autoriza alvo/limites e risco | US2, depois de AB-001/003 | Não é função comprovada do scanner estático |
| Entrevista/H1/H2/H3 | AGENTS, roteiro e templates | Obrigatório, Wemerson | Validação de estrutura pode ajudar futuramente | Regex/checkbox não comprova aprovação real |
| Rastreabilidade FR/RN/T/teste/commit/PR | Templates e ledger | Revisa ligação semântica | Futuro gate de links/IDs referenciados | Não adicionar novo job obrigatório sem desenho/aceite |
| Arquitetura hexagonal | Diretriz e núcleo parcial | Revisa imports/limite | Futura verificação incremental de dependências | Não refatorar transversalmente neste piloto |
| Política de emendas | Constituição 1.0.0 | PR separado, aprovação explícita | Futuro CODEOWNERS/estrutura | CODEOWNERS atual não cobre todos os documentos; exigência efetiva do ruleset não foi consultada |
| Rotação e limpeza de histórico | Docs #49/ADR-005; decisão local nesta sessão | Coordena e comprova | Scanner não executa rotação/rewrite | AB-002 e marco antes do gate final #99 |

## Divergências que devem ser corrigidas no recorte

- docs/ci-gates.md lista checks antigos e afirma inexistência de suíte frontend; usar o workflow efetivo como estado operacional.
- docs/secret-scanning-issue-106.md afirma Gitleaks/histórico ativo, mas o job usa somente PowerShell e fetch depth=1. Corrigir apenas com evidências da implementação real.
- A sessão delimitou histórico a develop/branch do PR; o documento antigo menciona todas as branches/tags. Registrar a mudança de recorte, sem alegar cobertura além das refs materializadas.
- Aceitação temporária local não autoriza reuse em homolog/produção, nem transformação de credencial real em falso positivo.

Nenhuma nova regra constitucional, dependência runtime, proteção de branch ou serviço AWS foi alterada por esta análise. Questões abertas por recorte estão no dossiê; as propostas de automação futura não são entregas desta fase.

## Estado após H1 US1

H1 aprovado: scanner histórico conectado ao CI, controles sintéticos RED/GREEN local e 18 exceções reais retiradas. Histórico real bloqueia por findings. H2 humano, CI remoto e remediação pendentes. Nenhum login/rotação/rewrite executado.

## Controles verificados na remediação — 2026-10-08

Automáticos: ambos os harnesses e scanner completo do candidato PASS; ausência/presença de DATABASE_PASSWORD coberta pelo npm run test:security já existente no CI; sete testes CMS e build Docker/Node 22 PASS. Seis demais jobs PASS apenas no run original 37818305235, não constitui aprovação do head reescrito.

Humanos pendentes: classificação da fixture JWT e janela excepcional de manutenção para as duas refs; autorização específica de ajuste mínimo/restauração da proteção e gestão de clones/demais refs. Develop exige PR, CI / Required Gate, up-to-date e conversas resolvidas; proteção inclui admins e impede force push. Nenhum controle de GitHub foi desligado. Ver history-remediation-plan.md.

Limites: ensaio não testa validade de credenciais, não revoga acesso, não limpa main/forks/caches/outros clones e não prova runtime da stack atualmente parada. O CI remoto do head final e os gates de integração continuam obrigatórios.
