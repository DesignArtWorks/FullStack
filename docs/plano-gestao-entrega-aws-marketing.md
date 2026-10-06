# Plano de gestão, entrega AWS e marketing do Escala

Referência: 06/10/2026 · Issue #114 · planejamento, sem declaração de go-live.

## Leitura da imagem

A imagem é uma publicidade de template de gestão de projetos. Seus elementos úteis são estrutura da equipe, termo de abertura, cronograma/marcos, dashboard, indicadores e riscos. Nomes, orçamento, percentuais e datas são exemplos do anúncio e não evidências do Escala. Não precisamos comprar o template para aplicar esses controles.

## Equipe e responsabilidades

Somos Wemerson e Codex. Os papéis abaixo são funções acumuladas, não pessoas contratadas nem revisão independente.

| Função | Wemerson | Codex |
| --- | --- | --- |
| Patrocínio e Product Owner | Decide visão, prioridade, orçamento e aceite | Estrutura opções, critérios e evidências |
| Gestão de projeto / análise de negócio | Valida problema e compromissos com clientes | Mantém backlog, dependências, marcos e riscos |
| Arquitetura / fullstack | Responsável técnico humano e revisor | Analisa, implementa e documenta incrementalmente |
| UX/UI e QA | Aprova experiência e realiza aceite com usuários | Propõe fluxos, cobre estados e executa validações |
| AppSec / plataforma / SRE | Responsável pela conta AWS e operação | Prepara IaC, gates, runbooks e evidências |
| Marketing / vendas / analytics | Conduz entrevistas, preços e relação comercial | Prepara mensagens, experimentos, conteúdo e métricas |

Codex não substitui parecer jurídico, validação trabalhista ou revisão independente. Custos AWS e mudanças produtivas devem seguir orçamento e aprovação de produção previstos nas ADRs. Execução futura requer tarefa concreta e acesso autorizado; este plano não cria monitoramento automático.

## Termo de abertura

**Problema:** PMEs dependem de planilhas e mensagens para construir e comunicar escalas mensais.

**Valor inicial:** escala mensal correta com templates, feriados, contadores, alertas e publicação auditável. Público prioritário inicial: restaurantes e pequeno varejo; escolha de foco é hipótese a validar com Wemerson e entrevistas.

**Escopo da primeira entrega:** jornada cadastro/login → organização/equipe → escala mensal → validação → publicação, isolamento de tenant, auditoria e operação recuperável na AWS. Marketing deve demonstrar apenas capacidades verificadas.

**Evolução posterior:** ponto completo, banco de horas avançado, dimensionamento e IA, conforme [roadmap](roadmap.md). Nenhuma promessa de REP-P, conformidade trabalhista ou economia percentual sem evidência.

**Fontes oficiais:** [posicionamento](Analise-Produto-Arquitetura-Concorrencia-Oceano-Azul.md), [OKRs](okr.md), [AppSec](appsec-criterios-de-aceite.md) e [ferramentas](ferramentas-ai-product-design-go-to-market.md). Este plano operacional complementa essas decisões.

## Marcos e cronograma condicionado

Planejar em ciclos semanais; datas de lançamento só após estimar capacidade de Wemerson e cumprir gates. Limite inicial de WIP: uma issue de implementação e uma atividade comercial. Falha crítica interrompe promoção.

| Marco | Trabalho / dependência | Evidência de saída |
| --- | --- | --- |
| M0 — organizar | #114; revisar backlog, responsáveis e capacidade semanal | Plano versionado, canais atualizados e prioridades confirmadas |
| M1 — liberar gate de segurança | #99, abertas #103/#106/#109; revalidar entregas fechadas | CI obrigatório, testes negativos/cross-tenant, scans e evidências sem blockers |
| M2 — prontidão de operação e cobrança | #52/#53/#54; #96/#86 conforme requisito de release | Restore medido, webhook idempotente, limites concorrentes, alertas e runbooks |
| M3 — homologação AWS | IaC conforme ADRs 005/006/007; orçamento atualizado | Rede/IAM revisados, smoke, carga, restore e rollback ensaiados |
| M4 — piloto controlado | M1–M3 e aceite da jornada mensal; #95/#98 conforme escopo do piloto | Usuários sintéticos/consentidos, onboarding e feedback, falhas tratadas |
| M5 — produção e aquisição | Aprovação de Wemerson, checks e branch protection | Release rastreável, monitoramento, suporte e campanha coerente com produto |

Discovery comercial e preparação de conteúdo podem ocorrer desde M0; aquisição paga e promessas de disponibilidade dependem de M5. Fechar issue não comprova implantação em AWS.

## AWS escalável e segura

Preservar [ADR-005](adr-005-secrets-producao-aws.md), [ADR-006](adr-006-infraestrutura-minima-producao-aws.md) e [ADR-007](adr-007-exposicao-servicos-producao.md): sa-east-1, Terraform, ECS Fargate, ALB/CloudFront/WAF, RDS PostgreSQL Multi-AZ privado, cache privado e ECR. Banco/usuário do CMS separados do backend; frontend usa Spring/BFF e nunca banco.

Sequência de execução: inventário e AWS Pricing Calculator → contas/ambientes/IAM/OIDC e state protegido → VPC/SGs/segredos → dados/backups → imagens por digest e services → DNS/TLS → homologação e ensaios → promoção aprovada. Não armazenar valores secretos no Git, IaC state ou logs.

Autoscaling deve começar com limites mínimos/máximos e métricas definidos por teste de carga; duas tasks de frontend/backend conforme ADR-006. Validar sessões, limites de conexão RDS e ausência de estado local antes de aumentar réplicas. Configurar target tracking e rollback de deploy compatíveis com o controller ECS utilizado. Fontes oficiais: [ECS target tracking](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/service-autoscaling-targettracking.html), [deployment circuit breaker](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/deployment-circuit-breaker.html).

Gates de homologação: negar Internet → dados/tasks, negar frontend → RDS, validar login/BFF e tenant negativo, TLS, health e OpenAPI restrito, restore isolado, rollback ao digest anterior e alarmes. Não há infraestrutura Terraform de produção confirmada nesta análise. Faixas históricas da ADR não são cotação atual; orçamento máximo, SLO e RPO/RTO ainda precisam ser aprovados e medidos.

## Marketing e vendas

Primeiro ciclo proposto: entrevistar cinco gestores do nicho inicial; registrar problema, alternativa atual e disposição para piloto. Meta de atividade, não resultado já alcançado. Codex prepara roteiro e síntese; Wemerson conduz relações e aprova publicação.

Mensagem: “Saia da planilha e monte sua escala mensal com feriados, contadores, alertas e publicação auditável”. Revisar cada capacidade na versão demonstrada. Matriz de claims: capacidade testada = comprovada; redução de retrabalho = hipótese; conformidade legal/percentuais = sem publicação até evidência.

Preparar uma landing segmentada, demonstração com dados sintéticos, FAQ, roteiro de onboarding e material de objeções. Strapi cuida de conteúdo; leads, consentimento e atribuição são do Spring/BFF. Não enviar dados de funcionários para ferramentas de marketing.

Funil: visita atribuída → lead consentido → demonstração/trial → primeira escala publicada → uso recorrente → pagamento. Começar com conteúdo orgânico e pilotos; mídia paga após ativação e disponibilidade comprovadas. Registrar experimento com público, hipótese, canal, custo máximo aprovado, janela e critério de decisão antes de gastar.

## Dashboard e indicadores

Todos começam sem baseline apurada; não reutilizar os 60%, R$ 24.500 ou 124 tarefas da imagem. Medir por release/coorte, sem misturar tenants identificáveis.

| Indicador | Definição / fonte | Cadência e critério |
| --- | --- | --- |
| Blockers de release | Issues bloqueadoras abertas + checks pendentes, GitHub | Semanal; zero pendência crítica para promoção |
| Lead time | Tempo Ready → integração, issues/PRs | Semanal; estabelecer baseline antes de meta |
| Falha de deploy | Deploys com rollback/incidente ÷ deploys | Por release; registrar denominador |
| Recuperação | Tempo de restore e perda de dados medida | Ensaio em homolog; comparar com RTO/RPO aprovados |
| Ativação | Tenants com primeira escala publicada ÷ tenants da coorte de trial | Semanal; janela proposta de 7 dias, validar instrumentação |
| Conversão | Leads consentidos ÷ visitas elegíveis; pagos ÷ trials elegíveis | Semanal/mensal; segmentar canal, excluir testes |
| Retenção | Tenants da coorte que publicam escala no mês seguinte ÷ ativados elegíveis | Mensal; baseline antes de meta |
| Custo AWS | Custo mensal e custo por tenant ativo, billing AWS | Semanal; teto aprovado e alarmes |

Instrumentação é trabalho posterior com finalidade, retenção, schema e testes. Não presumir que os eventos já existem.

## Riscos e resposta

| Risco | Responsável / resposta |
| --- | --- |
| Capacidade de uma pessoa e ausência de revisão independente | Wemerson: WIP limitado, marcos por evidência e revisão externa conforme risco |
| Cross-tenant, auth ou segredo exposto | Codex prepara testes/scans; Wemerson aprova resposta; bloquear release |
| Custo fixo AWS antes de receita | Wemerson aprova teto; Codex estima cenário e alarmes antes de provisionar |
| Backup não recuperável | Ensaio #52 com tempo e integridade registrados; não promover sem evidência |
| Cobrança duplicada ou limites contornados | #53/#54 com idempotência, concorrência e testes negativos |
| Marketing prometer produto/compliance não comprovados | Matriz de claims e revisão de conteúdo por Wemerson |
| Escopo disperso / UI em alterações locais | Preservar mudanças; priorizar jornada mensal e reconciliar #95/#98 |

## Cadência e Slack

GitHub é fonte de issues, PRs e evidências; docs são fonte de decisões. Slack recebe resumos com links, não uma cópia concorrente do backlog.

- `#escala-produto`: responsabilidades, marcos, prioridades, marketing, experimentos e KPIs. Uma thread por ciclo/decisão.
- `#escala-dev`: AWS, segurança, validações, release e issues fechadas. Linkar PR/checks quando disponíveis.
- `#escala-incidentes`: impacto, contenção, responsável, recuperação e retrospectiva somente para incidentes reais.
- `#all-escala-gestão-de-escalas`: anúncios de lançamento quando a release estiver comprovada.

Revisão semanal sugerida: prioridades/capacidade no início; demo, riscos e KPIs ao final. Em cada sessão autorizada, Codex atualiza evidências. Não foram agendadas mensagens nem automações.

## Snapshot do GitHub

Consulta em 06/10/2026: issues fechadas retornadas por filtro `is:issue is:closed`, ordenadas por atualização: #107, #108, #104, #105, #32, #31, #84, #97, #93 e #57. A ordem não afirma data de fechamento; o conector de busca não retornou timestamps de fechamento.

Pendências verificadas: #99/#103/#106/#109, #52/#53/#54 e observabilidade #96/#86. Checklists de épicos e roadmap podem estar defasados; estado individual e evidências de PR/CI devem ser reconciliados antes da promoção. Não foi realizado novo scan de segurança ou deploy nesta tarefa documental.

## Validação e reversão desta mudança

Somente este plano e o link no roadmap mudam; sem contratos REST, dependências ou runtime. Revisar links relativos e `git diff --check`; gates de CI continuam no PR. Reversão: revert do commit documental sem tocar dados ou infraestrutura. A issue #114 permanece aberta até integração e critérios de conclusão aplicáveis.
