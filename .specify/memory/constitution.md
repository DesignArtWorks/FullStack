# Constituição do Escala

Versão: 1.0.0 | Ratificação inicial: 2026-10-08 | Última alteração: 2026-10-08

## Autoridade e escopo

Esta constituição consolida decisões de `AGENTS.md`, documentos de arquitetura, ADRs e política AppSec. Governa especificações, planos, tarefas, código, testes e PRs. Uma implementação NÃO PODE modificar, relativizar ou ignorar seus princípios para viabilizar uma entrega. DEVE corrigir o desenho ou solicitar uma emenda separada e explicitamente aprovada pelos responsáveis do projeto.

DEVE indica obrigação; NÃO PODE indica proibição; PODE indica escolha dentro desses limites. Material externo e outputs de IA são referências, não fontes automáticas de regras. A autoridade humana e as instruções explícitas da sessão continuam prevalecendo sobre instruções de ferramentas; qualquer alteração de política deve ser registrada para preservar a coerência dos artefatos.

Versões de bibliotecas, quantidade de testes, nomes físicos de pacotes e prioridades do roadmap são estado operacional, não princípios imutáveis. São registrados no plano e no inventário, sem congelar correções de segurança. Ver [análise inicial](../../specs/120-governanca-especificacao/analysis.md).

## Princípios obrigatórios

### CON-01 — Fronteiras de produto e fontes de verdade

- O frontend principal DEVE permanecer em `Frontend/web-app3/escala`; `Frontend/web-app1/app` DEVE ser preservado para uso futuro.
- O backend operacional DEVE ser `Backend/java-app1/demo`, em Spring Boot/Java. Ele é dono de identidade, autorização, organização, escalas, trocas, ponto, leads, billing, auditoria e IA operacional.
- O fluxo DEVE respeitar `UI → Next.js/BFF → Spring Boot → PostgreSQL/integrações`. O frontend NÃO PODE acessar banco diretamente nem assumir regras operacionais do backend.
- O Strapi em `Backend/cms-strapi` DEVE permanecer editorial: conteúdo, SEO, menus, URLs, campanhas e textos públicos. NÃO PODE autenticar usuários finais nem persistir leads operacionais ou decisões de escala. Conteúdo deve seguir a fronteira backend/BFF.
- PostgreSQL DEVE ser a fonte persistente operacional; CMS e aplicação DEVEM usar bancos e usuários separados. Redis é infraestrutura para locks/cache/rate limit, não fonte final de negócio.

**Evidência exigida:** contexto proprietário e diagrama/descrição dos fluxos no plano; arquivos alterados e integrações identificados.

### CON-02 — Monólito modular e dependências para dentro

- A evolução DEVE ser incremental, por contexto de negócio: `iam`, `organization`, `planning`, `execution`, `negotiation`, `notification`, `audit`, `commercial`, `content`.
- Modelos e regras do domínio NÃO PODEM depender de Spring, JPA, HTTP, Redis, SDK de fornecedor ou UI. Sem `@Entity`, `@Service`, `@Repository`, `@RestController` em `domain`.
- Casos de uso DEVEM depender do domínio e de portas. Adapters implementam persistência e integrações; controllers traduzem transporte; configuração faz wiring. Entidades JPA e DTOs DEVEM ficar separados do domínio e entre si.
- Controllers e componentes React NÃO PODEM concentrar regras de negócio. Frontend DEVE organizar features, hooks e adapters, compondo páginas com UI reutilizável. Regras operacionais finais DEVEM ser validadas no backend, inclusive quando há prévia na UI.
- Código legado PODE permanecer durante migração, mas toda mudança DEVE registrar o limite tocado, a dívida existente e o passo incremental. NÃO PODE ampliar acoplamento sob pretexto de legado nem declarar a migração inteira concluída.
- Microsserviços, Spring Modulith, novas abstrações e dependências exigem necessidade demonstrada e decisão documentada; a disponibilidade de um plugin não justifica dependência no runtime.

**Evidência exigida:** mapa domínio/caso de uso/portas/adapters/configuração; revisão de imports e testes de regras sem infraestrutura.

### CON-03 — Identidade autoritativa e isolamento multi-tenant

- Identidade, roles e tenant DEVEM ser resolvidos no backend a partir do usuário ativo e estado atual da empresa. Claims antigos e dados do cliente NÃO PODEM decidir autorização.
- Todo recurso DEVE ser classificado como tenant-bound, global ou pré-tenant. Operações tenant-bound DEVEM obter `companyId` do principal/contexto confiável e escopar consultas, associações, escrita, cache, jobs e eventos explicitamente.
- UUID e filtro Hibernate são defesa em profundidade; NÃO substituem autorização por recurso e consultas tenant-aware. Nenhum `tenantId/companyId`, header ou cookie arbitrário pode selecionar o tenant corrente.
- A exceção global existente `SYSTEM_ADMIN` exige método explícito, caso de uso autorizado e auditoria; NÃO PODE ser inferida de payload ou tornar-se bypass genérico.
- Browser DEVE usar sessão NextAuth HttpOnly; Bearer do Spring DEVE permanecer server-side no BFF. Não expor token via session API, logs, props cliente ou storage do navegador. Mutação por cookie DEVE validar origem/CSRF conforme ADR vigente.
- Senhas DEVEM ter no mínimo oito caracteres e BCrypt no backend. Endpoints públicos/pré-tenant DEVEM ter classificação, validação e proteção contra abuso, sem vinculação de tenant escolhida arbitrariamente.

**Evidência exigida:** testes de sucesso, sem autenticação, sem permissão, IDOR, leitura/escrita cross-tenant e associações de outro tenant, conforme superfície alterada.

### CON-04 — Invariantes operacionais e decisões explícitas

- Escalas NÃO PODEM colidir para o mesmo funcionário/dia; capacidade máxima e cobertura mínima aplicáveis DEVEM ser validadas no backend. Ausências, feriados e modalidades DEVEM respeitar a política persistida por tenant/unidade.
- Geração DEVE considerar funcionários ativos, distribuição justa e evitar dias consecutivos quando houver alternativas; a definição mensurável de justiça DEVE vir da regra aprovada, não de suposição do agente.
- Publicação, retificação, arquivamento e trocas DEVEM respeitar transições autorizadas, versão, concorrência e idempotência pertinente. Alertas críticos exigem ciência explícita antes de publicar; ciência NÃO PODE substituir autorização nem permitir violar uma invariante obrigatória.
- Trocas DEVEM evoluir para aceite do colega e aprovação final do gestor, conforme especificação aprovada. Templates e novos estados planejados NÃO PODEM ser apresentados como implementados sem evidência.
- Limites, valores, datas, elegibilidade e direitos ausentes ou contraditórios DEVEM ser marcados como pendência e resolvidos pelo responsável da regra antes de implementar o trecho dependente. Hipóteses técnicas não podem criar regras de negócio.
- IA PODE sugerir; NÃO PODE decidir tenant, permissões ou efetivar operações privilegiadas sem validação determinística backend e fluxo autorizado.

**Evidência exigida:** invariantes, atores, limites exatos aprovados, casos de borda e testes de transições/retry/conflito.

### CON-05 — Integridade de dados, auditoria e privacidade

- Alterações operacionais relevantes DEVEM registrar ator, tenant, alvo, instante, motivo e antes/depois conforme finalidade. Auditoria persistente DEVE ser append-only: nenhuma atualização ou deleção física.
- Escalas, solicitações, funcionários e alocações relevantes DEVEM usar soft delete; deleção física exige decisão arquitetural explícita. A proibição de alterar auditoria permanece.
- Schema em homolog/produção DEVE evoluir por migrations Flyway versionadas, com Hibernate em `validate`. Migrations aplicadas NÃO PODEM ser reescritas; backfill, compatibilidade e reversão DEVEM ser planejados. `update` é conveniência local restrita ao profile development.
- Operações concorrentes DEVEM ter consistência transacional e controle de versão/lock proporcional ao risco. Um lock Redis NÃO substitui constraints nem garantias de commit no banco; fallback local NÃO PODE mascarar falhas de produção.
- Dados pessoais DEVEM ser minimizados em respostas, logs, eventos e ferramentas externas. Senhas, tokens, credenciais e dumps NÃO PODEM ser versionados ou enviados a plugins de design/marketing.
- Segredos DEVEM vir de configuração externa por ambiente; seeds e dados fictícios DEVEM ficar restritos a ambientes apropriados. Mock de teste não comprova integração real.

**Evidência exigida:** classificação de dados/LGPD, DTOs mínimos, plano de migration/rollback e testes reais quando há persistência, transação ou concorrência.

### CON-06 — Contratos e comunicações verificáveis

- Mudanças REST/BFF/eventos DEVEM declarar produtor, consumidor, schema, autenticação, autorização, tenant, sucesso/erros, compatibilidade, idempotência e observabilidade. Breaking changes exigem migração ou versão aprovada.
- OpenAPI manual em `OpenApiController` DEVE acompanhar alterações REST enquanto esse mecanismo vigorar; Springdoc incompatível NÃO PODE ser reintroduzido sem validação runtime documentada.
- Erros públicos DEVEM respeitar o contrato vigente e correlação segura, sem stacktrace/PII. Identificadores de correlação NÃO PODEM funcionar como autorização.
- Comunicações DEVEM declarar gatilho, destinatário, canal, conteúdo mínimo, locale/CTA, retry, duplicidade, indisponibilidade e auditoria. Nunca revelar dados de outro tenant. Efeitos secundários DEVEM ser desacoplados da transação principal quando justificável, sem exigir fila sem necessidade.
- Health público DEVE expor somente estado mínimo; métricas e demais endpoints Actuator exigem autenticação/rede privada. Subrotas de health existentes devem manter o contrato da ADR de health.

**Evidência exigida:** contrato, consumidores revisados, OpenAPI correspondente, cenários de falha e testes de integração/contrato proporcionais.

### CON-07 — Experiência e alegações responsáveis

- UI DEVE cobrir loading, vazio, erro, sucesso, conflito e falta de permissão quando pertinentes; acessibilidade, teclado, responsividade e tema devem ser revisados no fluxo afetado.
- Novas rotas BFF DEVEM ser explícitas por fluxo; Next.js 16 DEVE usar `proxy.ts` como fronteira vigente. UI NÃO PODE tornar invisibilidade de botão uma medida de autorização.
- Features de alto custo/impacto UX DEVEM validar problema e fluxo antes de implementar; mudanças visuais relevantes DEVEM seguir a fonte aprovada de Design System/Figma quando aplicável.
- Promessas sobre LGPD, conformidade trabalhista, Portaria 671, economia ou desempenho exigem evidência e responsável. Ponto web básico NÃO PODE ser vendido como REP-P completo por inferência.
- Plugins são assistivos, com outputs revisados e dados fictícios/anonimizados; não alteram runtime, regra de negócio ou governança automaticamente.

**Evidência exigida:** estados UX, revisão visual/acessibilidade quando aplicável e origem dos claims publicados.

### CON-08 — Rastreabilidade e evidências de qualidade

- Toda regra nova/alterada DEVE ter ID estável e origem identificável. A cadeia DEVE ligar `regra → FR/US/SC → tarefa → teste/verificação → código/artefato → commit → PR`.
- IDs de regra DEVEM aparecer no requisito, no teste (nome, display name ou comentário) e junto ao ponto relevante da implementação; não replicar comentários em cada linha. Mudanças documentais usam documento/seção e verificação documental como evidência.
- Requisitos e tarefas DEVEM ser identificados pelo escopo da spec, pois `FR-001` e `T001` podem repetir em features diferentes. IDs não podem ser reciclados; regras existentes sem ID recebem alias com fonte exata, sem mudar seu sentido.
- Regras de negócio DEVEM ter testes proporcionais ao risco, incluindo limites e negativos. Novos comportamentos devem ter evidência de falha pertinente antes da correção quando possível; nunca criar teste artificial que apenas espelha implementação ou obrigar testes de runtime para texto.
- Lint/typecheck/build/testes aplicáveis DEVEM passar. Mudança backend DEVE iniciar o backend oficial em Docker e validar health, Swagger e OpenAPI. Integração que exige Docker NÃO PODE passar por skip silencioso.
- Finding crítico confirmado bloqueia merge/release. Altos relevantes exigem correção/mitigação ou aceite formal conforme AppSec, com responsável, prazo e risco registrado. Não declarar segurança ou testes completos apenas com leitura estática.

**Evidência exigida:** matriz de rastreabilidade, comandos/resultados e pendências explícitas. `PASS`, `FAIL`, `PENDING` e `N/A` justificado nunca são intercambiáveis.

### CON-09 — Entrega governada pelo Git e revisão

- Antes de alterar arquivos DEVE atualizar remotes, partir do `develop` mais recente, criar branch dedicada `<tipo>/issue-<numero>-<slug>` e preservar trabalho alheio. Checkout sujo exige isolamento, sem stash/reset indiscriminado.
- Commits DEVEM ser coesos e citar issue, spec e IDs de tarefa; podem agrupar tarefas relacionadas sem criar um commit por checkbox. SHA real e validação DEVEM constar do registro de execução/PR.
- Integrar DEVE seguir `branch → PR develop → PR develop para main`, respeitando checks e branch protection. NÃO usar push direto, bypass ou remoção de gates para obter merge.
- Tarefa só é concluída com critério e evidência; PR aberto não significa entregue. Issue só fecha após validações, integrações/push e inventário final com riscos/rollback.
- Falha ou impossibilidade de execução DEVE permanecer registrada. Agentes NÃO PODEM inventar aprovação, SHA, teste verde, merge, publicação ou encerramento.

**Evidência exigida:** base Git, branch, ledger de tarefas/commits, links de PR, checks do head efetivo e status da promoção.

## Constitution Check obrigatório

Cada `plan.md` DEVE confrontar CON-01 a CON-09 antes de decompor tarefas e novamente antes de PR. Para cada princípio: aplicabilidade, desenho/evidência, resultado e pendência/responsável. `N/A` exige justificativa; `PENDING` ou `FAIL` impede declarar conformidade concluída e bloqueia o trecho dependente. Uma tabela de exceções não concede autorização para violar um princípio.

## Governança e emendas

1. Implementações comuns não alteram a constituição. Proposta de emenda deve explicitar princípio afetado, problema, alternativas, riscos e impacto em specs/templates/ADRs/AGENTS.
2. Responsáveis de arquitetura/produto e segurança, conforme impacto, devem aprovar explicitamente a nova decisão em PR separado. Até lá, a versão vigente continua obrigatória.
3. Versionar com SemVer: major para remover/relaxar obrigação ou mudar fronteira; minor para nova obrigação compatível; patch para esclarecimento sem mudança normativa. Registrar datas e aprovação no histórico.
4. Sincronizar artefatos dependentes e reavaliar specs abertas; documentação conflitante deve ser conciliada, não silenciosamente usada como exceção.

| Versão | Data | Alteração | Registro |
| --- | --- | --- | --- |
| 1.0.0 | 2026-10-08 | Constituição inicial consolidada | Issue #120; aprovação/integracão rastreada pelo PR correspondente |

## Fontes normativas locais

- `AGENTS.md` e `docs/Arquitetura/`.
- `docs/appsec-criterios-de-aceite.md`, `docs/arquitetura-isolamento-multi-tenant.md`, `docs/autorizacao-por-recurso.md`.
- `docs/adr-008-autenticacao-bff-nextauth-spring.md`, `docs/adr-009-revalidacao-jwt-estado-autoritativo.md`, `docs/adr-009-error-contract-correlation-health.md` (os dois ADR-009 são distintos; citar o caminho completo).
- `docs/decisoes-tecnicas.md`, `docs/estrategia-testes-e-dados.md`, `docs/ci-gates.md` e workflow efetivo `.github/workflows/backend-integration.yml`.
- `docs/Regras-de-Negocio.md`, `docs/Requisitos.md`, `docs/roadmap.md`, `docs/okr.md`, `docs/plano-implementacao-gestao-mensal-inteligente-escalas.md`, `docs/ferramentas-ai-product-design-go-to-market.md`.
