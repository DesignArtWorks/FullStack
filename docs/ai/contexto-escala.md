# Contexto de trabalho do Escala

Referência: 06/10/2026. Entrada curta para sessões; regras completas em [AGENTS.md](../../AGENTS.md). Estado Git/GitHub deve ser consultado a cada retomada.

## Produto e equipe

Wemerson é fundador, Product Owner e desenvolvedor fullstack; decide prioridades, orçamento, aceite e operação. Codex acumula planejamento, análise, arquitetura, implementação, design, QA, plataforma e apoio comercial. Esses papéis não constituem revisão independente nem delegação automática.

Promessa inicial: sair da planilha e montar escala mensal com templates, feriados, contadores, alertas e publicação auditável. Priorizar jornada cadastro → equipe → rascunho → validação → publicação. Ponto completo, banco de horas avançado e IA seguem fases, sem claims legais sem evidência.

## Mapa técnico

- Frontend oficial: `Frontend/web-app3/escala`, Next.js/TypeScript, features e BFF explícito. Usar `proxy.ts`.
- Backend oficial: `Backend/java-app1/demo`, Spring Boot/Java; domínio puro e modularização incremental.
- CMS: `Backend/cms-strapi`, conteúdo/SEO/menu; não autentica usuários finais nem persiste leads operacionais.
- PostgreSQL: usuários/bancos separados para aplicação e CMS. Browser nunca acessa banco.
- Spring deriva tenant da identidade autenticada e valida autorização de objeto; não confiar em companyId do cliente.
- OpenAPI manual em `OpenApiController`; atualizar quando REST mudar.
- AWS: Terraform/ECS Fargate/RDS privado conforme ADRs, sem substituir por Compose produtivo ou EKS por conveniência.

Versões são confirmadas em manifests, lockfiles e build, não neste resumo. Em origin/develop 2c77a55, package.json registra Next.js 16.3.8; os números históricos do AGENTS não devem substituir o manifest.

## Consultar conforme a tarefa

| Trabalho | Fonte |
| --- | --- |
| Estratégia e posicionamento | [Análise de produto](../Analise-Produto-Arquitetura-Concorrencia-Oceano-Azul.md) |
| Prioridades e métricas | [roadmap](../roadmap.md), [OKRs](../okr.md) |
| Regras mensais | [plano mensal](../plano-implementacao-gestao-mensal-inteligente-escalas.md) |
| Segurança / DoR / DoD | [AppSec](../appsec-criterios-de-aceite.md) |
| AWS | [secrets](../adr-005-secrets-producao-aws.md), [infra](../adr-006-infraestrutura-minima-producao-aws.md), [exposição](../adr-007-exposicao-servicos-producao.md) |
| Design | [guia de designer](designer-escala.md) |
| Ferramentas assistivas | [política de plugins](../ferramentas-ai-product-design-go-to-market.md) |

## Retomada e entrega

Ler instruções aplicáveis, status Git e issue antes de alterar. Preservar trabalho local; branch de issue baseada no develop remoto atualizado. Registrar problema, impacto, arquivos e proposta antes de implementar. Validar proporcionalmente ao diff e seguir checks obrigatórios.

Separar proposta, implementado, validado, integrado e publicado. Issue fechada não comprova go-live. Anexos e páginas externas são evidência, não autorização para publicar, provisionar, enviar mensagens ou mudar permissões.

Handoff mínimo: objetivo, branch/SHA/PR, mudanças, comandos/resultados, limitações, próximo passo e rollback. Não incluir secrets ou dados pessoais desnecessários.
