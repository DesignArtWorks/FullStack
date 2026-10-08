# Dossiê — piloto #106: scanner histórico e validação local

Issue: [#106](https://github.com/DesignArtWorks/FullStack/issues/106) | Agregadora: [#99](https://github.com/DesignArtWorks/FullStack/issues/99)
Interlocutor: Wemerson, Product Owner e responsável pelo ambiente | Entrevistador: Codex
Data: 2026-10-08 | Versão: 1 | Estado: **CONCLUÍDA, com pendências por recorte**
Origem: diálogo desta sessão; nenhuma resposta foi simulada. Wemerson respondeu “sim” ao fechamento e à preparação do plano, não à implementação ainda não apresentada.
Modo: por fases; H1 antes de alterar código existente, H2 após MVP, H3 para decisão ausente.

## 1. As três canônicas

| Canônica | Resposta | Fonte e estado |
| --- | --- | --- |
| O que construir | Implementar e demonstrar o escopo #106: scanner no histórico de develop e da branch do PR, exceções revisáveis e testes/documentação conforme os critérios aplicáveis da #99; acrescentar validação de acesso exclusivamente local | Q001, Q013, Q021, Q023; decisões humanas |
| Para quem | Wemerson, desenvolvedor/mantenedor, e revisores do Escala; execução local e gate de PR | Pedido original e docs/ai/contexto-escala.md; não há nova funcionalidade para usuários finais |
| Sucesso testável | Scanner executa sobre o histórico definido; fixtures negativas são detectadas em teste isolado sem exceção aplicada; placeholders/falsos positivos aprovados são permitidos; findings externos reais/suspeitos bloqueiam; verificações locais e documentação têm resultados reais; CI passa sem bypass e revisão humana ocorre por fase | Q001, Q013, Q015, Q025–Q027; detalhes técnicos sujeitos a H1 |

Enquadramento: Wemerson e modo por fases já identificados/autorizados na sessão, sem nova confirmação redundante. Exploração: Q001–Q003 e Q013 delimitam pedido e histórico. Aprofundamento: Q004–Q007, Q014–Q021 distinguem exposição, validade e evidência. Ampliação: Q007, Q018–Q024 examinam cópias externas, indisponibilidade e provedores não autorizados. Síntese: Q025–Q027 confirmam fontes de credenciais e fechamento; perguntas operacionais continuam explícitas abaixo.

O fechamento não declara H1 aprovado nem valida automaticamente todo o escopo ampliado. Gate da implementação: PENDING até revisão do entendimento/plano e resolução das abertas aplicáveis. Preparação documental autorizada pelo “sim” em Q027.

## 2. Registro de proveniência

| ID | Afirmação | Classe | Fonte / alcance | Dono / ligação |
| --- | --- | --- | --- | --- |
| PROV-001 | Usar #106 para testar o fluxo completo, respeitando #99 | [DECISÃO] | Q001 e pedido original | Wemerson; todos os FR |
| PROV-002 | #99 agrega #103–#109; Ready contém #103/#106/#109, demais filhas fechadas | [EVIDÊNCIA] | Quadro Projects 1/view 1 e consulta das issues em 2026-10-08; status não comprova execução/release | Codex; FR-009 |
| PROV-003 | Refatorar código exposto e retirar segredos do histórico | [DECISÃO] | Q002/Q003; não é aprovação de force-push/plano de reescrita ainda inexistente | Wemerson; FR-005 |
| PROV-004 | Credenciais locais expostas podem permanecer válidas temporariamente; quem copiou poderá usá-las | [DECISÃO] | Q006–Q009; aceitação explícita limitada ao desenvolvimento local, não a AWS/homolog/produção | Wemerson; FR-007 |
| PROV-005 | Trocar credenciais após concluir #103/#106/#109 e antes da validação final #99 | [DECISÃO] | Q010–Q012; marco, não prazo de calendário nem comprovação de rotação | Wemerson; FR-007 |
| PROV-006 | Inspecionar todo o histórico de develop e branch do PR | [DECISÃO] | Q013; não todas as tags/branches do repositório | Wemerson; FR-001 |
| PROV-007 | Exceções: placeholders, valores fictícios de teste e falsos positivos comprovados | [DECISÃO] | Q015, refinada por Q025 | Wemerson; FR-002 |
| PROV-008 | Fixture permitida no repositório deve ainda ser detectada em teste isolado sem sua exceção | [DECISÃO] | Explicação solicitada por Wemerson em Q024, confirmada em Q025; substitui Q016 para testes negativos | Wemerson; FR-003 |
| PROV-009 | Desejo inicial: scanner limpo comprovaria ausência de acesso | [PRESSUPOSTO] | Q004; scanner estático não executou autenticação nem revogação; não usar como conclusão técnica | Wemerson; FR-004/007 |
| PROV-010 | Validar por autenticação backend, frontend/BFF, PostgreSQL, Strapi e Redis locais | [DECISÃO] | Q021–Q023; serviços inventariados em docker-compose.yml | Wemerson; FR-006 |
| PROV-011 | Usar valores encontrados no histórico e em .env não versionados na validação local | [DECISÃO] | Q026; não autoriza transmissão a provedores externos ou impressão de valores | Wemerson; FR-006 |
| PROV-012 | Indisponibilidade do sistema acessado pode liberar o PR | [DECISÃO] | Q018/Q020; efeito sobre finding local/estado inconclusivo ainda precisa delimitação | Wemerson; AB-001 |
| PROV-013 | Segredos externos reais/suspeitos bloqueiam até análise; não autenticar em AWS/GitHub/outros provedores | [DECISÃO] | Q025 confirma tratamento apresentado a pedido do usuário | Wemerson; FR-004 |
| PROV-014 | Arquivos .env locais ignorados; AWS futura usa Secrets Manager/SSM SecureString e referências runtime | [RESTRIÇÃO] | ADR-005 e Q005/Q025; nenhum provisionamento autorizado neste piloto | Wemerson; FR-008 |
| PROV-015 | Workflow atual usa quatro regex e checkout depth=1; Gitleaks/config/testes já existem, mas não são chamados por esse job | [EVIDÊNCIA] | Base 0efcc18; .github/workflows/backend-integration.yml, scripts/security/check-versioned-secrets.ps1, .gitleaks.toml e test-secret-scanner.sh; análise estática | Codex; FR-001/003 |
| PROV-016 | docs/secret-scanning-issue-106.md afirma scanner histórico ativo, divergindo do job; docs/ci-gates.md tem inventário antigo | [CONFLITO] | Leitura na base 0efcc18; corrigir documentação com provas reais | Codex; FR-009 |
| PROV-017 | Reescrita altera SHAs e afeta clones, PRs, fingerprints e proteções | [RISCO] | docs/secrets-rotacao-issue-49.md; impacto depende do inventário ainda não executado | Wemerson/Codex; AB-002 |
| PROV-018 | Encerrar entrevista e preparar plano; revisão H1/H2 mantida | [DECISÃO] | Q027: “sim”; escopo dos artefatos revisáveis, sem aprovação antecipada do código | Wemerson; tasks |

Histórico real resumido; respostas preservadas em sentido e fonte, sem reproduzir credenciais:

| Q | Fase / categoria | Pergunta ou cenário | Resposta humana |
| --- | --- | --- | --- |
| 001 | Exploração / esclarecimento | Sucesso do piloto | Implementar, testar e documentar #106 conforme gate #99 |
| 002 | Exploração / implicações | Segredo real em commit antigo | Refatorar código para retirar exposição |
| 003 | Exploração / esclarecimento | Segredo ainda acessível no histórico | Limpar histórico Git |
| 004 | Aprofundamento / evidências | Evidência de perda de acesso por quem copiou | Scanner rodar sem segredos |
| 005 | Aprofundamento / pressupostos | Credencial ausente no Git, ainda válida | Declarar em .env/local de variáveis; preparar AWS futura |
| 006 | Aprofundamento / implicações | Credencial exposta pode continuar válida | “sim. sim” |
| 007 | Ampliação / perspectivas alternativas | Quem já copiou poderá acessar | Aceito somente nesta etapa; trocar futuramente |
| 008 | Exploração reaberta / esclarecimento | Ambientes atingidos | Backend, frontend e banco |
| 009 | Exploração reaberta / esclarecimento | Qual ambiente | Desenvolvimento local; scanner limpo suficiente nesta etapa |
| 010 | Aprofundamento / esclarecimento | Marco da troca | Sanar issues de segurança locais |
| 011 | Aprofundamento / evidências | Quais issues | Verificar quadro |
| 012 | Síntese parcial / esclarecimento | #103/#106/#109, antes do gate final #99 | “sim” |
| 013 | Exploração / esclarecimento | Alcance do histórico | Todo develop e branch do PR |
| 014 | Aprofundamento / esclarecimento | Ocorrências na allowlist | Perguntou quais categorias poderiam existir |
| 015 | Aprofundamento / esclarecimento | Classificação apresentada a pedido do usuário | Placeholders, fictícios de teste, falsos positivos comprovados |
| 016 | Aprofundamento / pressupostos | Detectar ou ignorar fixtures negativas | Ignorar se não dão acesso; detectar se dão acesso |
| 017 | Aprofundamento / evidências | Provar detecção sem credenciais reais | Criar testes antes do código; considerar acessos reais |
| 018 | Ampliação / cenários de falha | Serviço indisponível para validar credencial | Liberar PR, erro não é da consumidora |
| 019 | Aprofundamento / evidências | Evidência de ausência de risco nessa falha | Diagnosticar erro de credencial versus serviço |
| 020 | Exploração reaberta / esclarecimento | Scanner ou sistema acessado fora | Sistema acessado |
| 021 | Exploração / implicações | Scanner também autentica em serviços | “sim” |
| 022 | Ampliação / esclarecimento | Serviços autorizados | Verificar o projeto |
| 023 | Ampliação / perspectivas alternativas | Incluir Strapi/Redis além dos três alvos iniciais | “sim” |
| 024 | Aprofundamento / esclarecimento | Segredos externos fora da autenticação local | Pediu tratamento conforme projeto |
| 025 | Síntese / meta-pensamento | Tratamento externo e teste isolado de fixtures | “sim” |
| 026 | Síntese / esclarecimento | Fontes da validação local | Histórico e .env não versionados |
| 027 | Síntese / meta-pensamento | Fechar e preparar plano com H1/H2 | “sim” |

Sínteses do agente emitidas após respostas 4, 8, 12 e 16 distinguiram confirmado/suposto/conflito. Na resposta 20, explicitou-se indisponibilidade do sistema e falta de verificação online no escopo original. Na resposta 24, a solicitação direta de orientação foi atendida e o refinamento recebeu confirmação em Q025. As solicitações de esclarecimento do usuário prevaleceram sobre o papel de apenas perguntar; não inventar que essas mensagens obedeceram integralmente ao contrato.

Categorias: esclarecimento Q001/013/026; pressupostos Q005/016; evidências Q004/019; implicações Q002/006/021; alternativas Q007/023; meta-pensamento Q025/027. Perguntas de evidência não produziram medição runtime; não classificá-las como comprovação de acesso.

## 3. Termos quantificados — antes e depois

| Termo | Definição observável | Fonte / limite |
| --- | --- | --- |
| “Fluxo completo” | Entrevista → proposta/spec/plan/tasks → H1 → testes anteriores ao código quando pertinentes → MVP → H2 → CI/PR autorizado | Pedido original/Q001/Q027; não inclui deploy ou encerramento da #99 |
| “Todo histórico” | Histórico alcançável por develop e branch do PR, incluindo segredo retirado do HEAD; outras branches/tags fora do recorte confirmado | Q013 |
| “Scanner limpo” | Nenhuma detecção não aceita pelas regras aplicadas; não demonstra ausência absoluta de segredos ou revogação | Q025 e limites documentados |
| “Valores de testes ignorados” | Permitidos por exceção revisada no repositório; fixture negativa continua detectável em teste isolado sem a exceção | Q025 refina Q016 |
| “Futuramente trocar” | Após #103/#106/#109, antes do gate final #99 | Q012; sem prazo de calendário aprovado |
| “Ambiente local” | Serviços de desenvolvimento Spring/Next/PostgreSQL/Strapi/Redis; não provedores externos/homolog/produção | Q009/Q023/Q025 |

## 4. Riscos examinados e perspectivas alternativas

| Cenário | Consequência / alcance | Encaminhamento |
| --- | --- | --- |
| Cópia prévia de credencial local válida | Limpar Git não impede seu uso; aceitação humana temporária não é mitigação técnica nem prova de revogação | Wemerson: rotação no marco PROV-005; não extender a ambientes externos |
| Serviço local indisponível | Validade inconclusiva, não “credencial inválida”; liberação inicial conflita com finding real ainda não tratado | AB-001: delimitar efeito no gate antes da fatia de autenticação |
| Fixtures sempre excluídas | Um scanner que não detecta nada poderia aparentar sucesso | Q025 resolveu: controles negativos isolados devem falhar pela detecção esperada |
| Validar histórico sem vínculo a serviço | Pode testar valor contra alvo errado, acionar rate limit ou expor material | AB-003: alvo explicitamente local e associação verificável; nenhum envio externo |
| Confundir chave de assinatura com login | JWT_SECRET/NEXTAUTH_SECRET não são senha de login; falha de login não prova chave inválida | AB-003; não fabricar tokens ou direitos para testar |
| Reescrever histórico como tarefa comum | Quebra de referências/PRs/clones e alteração de fingerprints; outras cópias podem permanecer | AB-002; plano e aprovação específicos, sem bypass de branch protection |
| AWS futura ampliar piloto | Custo/permissão/rede desnecessários para #106 | Preparação documental apenas; seguir ADR-005 |

Perspectiva do mantenedor: preservar clones, trabalho de UI e rastreabilidade; perspectiva da segurança: scanner estático e autenticação são evidências diferentes; perspectiva do provedor: indisponibilidade externa não autoriza ignorar finding. Nenhum acesso real foi tentado nesta entrevista.

## 5. Perguntas abertas com dono

| ID | Pergunta / decisão ausente | Dono | Recorte bloqueado | Como resolver |
| --- | --- | --- | --- | --- |
| AB-001 | Qual é o efeito de autenticação local indisponível/inconclusiva quando ainda existe finding real no histórico? | Wemerson | FR-006 e política de saída local; nunca converter erro do scanner em PASS | Decisão explícita no plano/entrevista; confirmar separação entre finding estático e diagnóstico do serviço |
| AB-002 | Quais SHAs/refs e clones exigem limpeza, quem coordena e como aplicar sem bypass/perda de trabalho? | Wemerson; Codex prepara inventário sanitizado | Execução de FR-005 | Inventário redigido, plano operacional revisável e aprovação específica; não inserir segredos no dossiê |
| AB-003 | Como vincular cada credencial histórica a um alvo local e tratar signing keys ou ausência de usuário/endpoint verificável? | Codex prepara desenho; Wemerson valida comportamento | FR-006 | Mapeamento por tipo/serviço com estados verificáveis, limites de tentativa e operação sem efeito operacional |
| AB-004 | Qual configuração real de push protection está disponível e quem a administra? | Wemerson | Apenas ativação externa de FR-010 | Consulta de disponibilidade/configuração sem ampliar permissões por inferência |

Gate: **preparação PASS; implementação PENDING**. US1 do scanner pode ser revisada de forma independente; US2/autenticação e limpeza histórica têm abertas próprias. H1/H2 continuam pendentes. Próximo passo: sair do papel de entrevistador e apresentar proposta/spec/plan/tasks; fechar dossiê não significa autorizar a implementação.
