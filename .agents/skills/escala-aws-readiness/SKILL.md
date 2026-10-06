---
name: escala-aws-readiness
description: Avaliar prontidão de uma release do Escala na AWS e preparar plano de homologação ou deploy conforme ADRs existentes. Use para infraestrutura e operação AWS do Escala.
---

# escala-aws-readiness

Leia docs/ai/contexto-escala.md, ADRs 005/006/007 e docs/appsec-criterios-de-aceite.md a partir da raiz. Preserve o baseline Terraform/ECS Fargate e dados privados.

Confirme ambiente, conta/região quando acessíveis, código IaC existente, digests, migrações, evidências de checks e orçamento. Informação não disponível vira gap, não recurso assumido. Preços e recomendações AWS exigem fonte oficial atual e premissas.

Entregue plano com IAM/OIDC/state, rede/SGs, segredos por ambiente, banco/CMS separados, backup/restore, autoscaling baseado em carga e conexões, observabilidade e rollback por digest. Não introduza EKS/microserviços sem necessidade demonstrada e decisão arquitetural.

Valide gates: acesso negado à rede/dados, testes negativos tenant/auth, health e OpenAPI conforme exposição autorizada, restore medido, rollback ensaiado e custo aprovado. Distinga planejado, provisionado e testado.

Avaliação não concede autorização para apply, alteração de dados, custos ou publicação. Se o usuário já autorizou uma ação concreta, execute dentro desse escopo sem reconfirmação artificial; aprovação produtiva prevista nas ADRs continua aplicável. Não leia/imprima valores secretos para construir inventário.

Registre bloqueadores com evidência e próximo passo. Não declare production ready por Docker local ou issues fechadas.
