---
name: escala-issue-delivery
description: Implementar e validar issues do Escala com contratos, isolamento de tenant e fluxo Git do projeto. Use para entrega de código ou documentação de uma issue.
---

# escala-issue-delivery

Leia AGENTS.md e docs/ai/contexto-escala.md na raiz do checkout; instruções mais específicas se aplicam aos diretórios alterados.

Inspecione issue, código e testes. Antes de editar, declare problema/ator, contratos, invariantes, impacto tenant/LGPD, arquivos e implementação proporcional. Não duplique regras de negócio em controller ou React.

Atualize origin e crie branch de issue baseada em develop no formato do AGENTS. Preserve mudanças existentes; use checkout isolado quando necessário. Não inclua alterações de outra tarefa.

Para dados tenant-bound, derive companyId do principal e teste negativa/cross-tenant. Mudança REST exige avaliar consumidores e atualizar OpenApiController. Escolha testes que provem a regra e casos de concorrência relevantes.

Execute os gates aplicáveis: testes, lint/typecheck/build e, quando backend mudar, Docker oficial com health/Swagger/OpenAPI. Para diff exclusivamente documental, valide links/estrutura/diff e registre por que runtime não foi executado; checks obrigatórios do GitHub continuam valendo.

Publique/integre somente no escopo autorizado e via PR/checks; não force bypass. Fechar issue exige inventário e evidências das integrações exigidas pelo AGENTS. Se o pedido limita o destino a develop, não promova main por inferência. Reporte validação limitada, conflitos ou checks bloqueadores com causa e próximo passo.

Entregue branch/SHA/PR, resultado, riscos e rollback. Não declare deploy a partir de merge.
