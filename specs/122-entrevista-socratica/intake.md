# Registro de origem da adoção — não é entrevista concluída

Issue: #122 | Data: 2026-10-08 | Responsável: Wemerson | Autor do registro: Codex

Esta issue cria o próprio processo obrigatório para implementações posteriores à adoção. Não houve entrevista completa de cinco fases/seis categorias nesta sessão. Este registro preserva as instruções reais e a única pergunta de preferência, sem gerar um `interview.md` VALIDADO fictício.

| ID | Afirmação / instrução | Classe | Origem / limite |
| --- | --- | --- | --- |
| PROV-001 | Integrar PR #121 em develop antes da nova documentação | [DECISÃO] | Pedido explícito de Wemerson nesta sessão; main não solicitado |
| PROV-002 | Criar entrevista socrática e executar antes de qualquer implementação | [DECISÃO] | Pedido explícito de Wemerson com prompt e texto de referência |
| PROV-003 | Uma pergunta por mensagem, fases/categorias, termos quantificados e dossiê | [RESTRIÇÃO] | Contrato de entrevista explicitamente fornecido pelo usuário |
| PROV-004 | Execução por fases com revisão antes de alterar código existente e após MVP | [DECISÃO] | Resposta humana à pergunta de preferência abaixo |
| PROV-005 | PR #121 integrado em develop | [EVIDÊNCIA] | GitHub retornou merged=true; SHA 91484273f9ea473ce38d0c6e3364d62c9a12315b |
| PROV-006 | O workflow do head corrigido do PR #121 passou | [EVIDÊNCIA] | Monorepo CI run 37790079431; oito jobs em success |
| PROV-007 | O método vai reduzir defeitos futuros | [PRESSUPOSTO] | Benefício esperado; não foi medido no Escala |
| PROV-008 | Incorporar contexto técnico, estrutura/apoios e decomposição verificável por fases | [RESTRIÇÃO] | Material adicional plan/tasks/implement e imagens enviados nesta sessão; adaptação local sem instalar CLI |

Pergunta real: "Para as próximas implementações, qual modo de execução você quer adotar como padrão?"

Resposta real de Wemerson: "Por fases, com revisão humana antes de alterar código existente e após o MVP."

Canônicas da adoção, derivadas do pedido (não de entrevista simulada): criar roteiro/dossiê e vínculo ao fluxo; para Wemerson, Codex e revisores do Escala; sucesso demonstrado por documentação utilizável e referências obrigatórias nos pontos de entrada. A validação humana do MVP será registrada no checkpoint H2 desta issue.

Sem novas regras funcionais de escala, contrato REST, schema ou autorização. Perspectiva alternativa considerada no desenho: um texto estático não se executa sozinho; AGENTS/templates obrigam a leitura/diálogo pelo agente, sem alegar hook/cron/serviço. Risco: confundir um dossiê produzido com aprovação; estados explícitos e fonte humana mitigam.

Aberta: aprovação humana do MVP documental, dona Wemerson, bloqueia integração da nova política nesta fase. Material Vetor e imagens são didáticos, não confirmação de dados reais ou comandos instalados.
