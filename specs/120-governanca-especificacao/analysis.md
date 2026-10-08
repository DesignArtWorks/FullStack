# Análise de base do projeto

Data: 2026-10-08 | Base: `origin/develop` em `08d0dcdb8646804b9a8913b5ea94d36dc3199890`

## Método e limite

Inventário transversal do monorepo e leitura dirigida dos documentos oficiais, manifests, configuração, workflow e exemplos de código/testes. Esta é análise arquitetural para governança: não é auditoria linha a linha, scan de segurança ou comprovação runtime. Nenhum build/teste funcional foi executado nesta etapa documental. O checkout principal contém trabalho local de UI; a entrega está isolada em `.worktrees/issue-120`.

## Inventário observado

| Área | Evidência local | Decisão preservada / impacto |
| --- | --- | --- |
| Backend oficial | `Backend/java-app1/demo/pom.xml` | Spring Boot 4.1.0, Java 25; overrides atuais incluem Spring Framework 7.0.9, Jackson 3.1.7, Tomcat 11.0.25. Versões são baseline, não norma imutável. |
| Frontend oficial | `Frontend/web-app3/escala/package.json`, `pnpm-lock.yaml` | Next 16.3.8, React 19.2.6, TypeScript ^5.9.3, pnpm 10.33.3; scripts lint/typecheck/build e Playwright existentes. |
| CMS | `Backend/cms-strapi`, schemas e configuração | Conteúdo editorial; não assumir domínio operacional nem autenticação final. |
| Banco e runtime | `docker-compose.yml`, `Data/postgres/init-multiple-databases.sh` | PostgreSQL 16, bancos/usuários separados, Redis, rede comum e healthchecks; segredos por ambiente. |
| Domínio puro parcial | `core/contact/domain`, `core/contact/usecase/SubmitContactUseCase.java`, `core/contact/port`, `core/scheduling/domain/WorkShiftDomain.java` | Há modelos/portas/casos de uso separados; reutilizar a direção de dependência. |
| Migração incompleta | `service/`, `entity/`, `repository/`, `core/scheduling/application/GenerateScheduleService.java` | Convivência de serviços Spring/JPA e núcleo puro; nem toda pasta core significa independência de framework. Migrar por caso de uso. |
| Identidade/tenant | `security/TenantContext.java`, `AuthenticatedUserPrincipal.java`, filtros e `service/AuthoritativeIdentityService.java` | Principal server-side, consultas escopadas e estado atual; UUID/filtro não substituem autorização. |
| Sessão/BFF | `src/lib/auth.ts`, `src/lib/auth/server-auth.ts`, `src/lib/bff/backend.ts`, `src/proxy.ts` | Cookie HttpOnly no browser, Bearer interno, proteção de origem e erros/correlação; preferir BFF explícito. |
| Escala mensal | `ScheduleCyclePublicationService.java`, `ScheduleCycleAssignmentService.java`, testes correspondentes, `src/features/escala-inteligente` | Publicação/retificação/arquivamento e ciência de alertas já têm implementação; outras etapas do roadmap precisam de evidência antes de anunciar. |
| Dados | `src/main/resources/application.yml`, `db/migration/` | Flyway já adotado; validate em base/homolog/production e update restrito a development. |
| Contratos | `controller/OpenApiController.java`, `ApiError.java`, `docs/api/swagger-openapi.md` | OpenAPI manual e contrato público de erro; não reintroduzir Springdoc sem validar. |
| Testes | `src/test/java/.../service`, `.../security`, `.../integration`, frontend Playwright | Unitários existentes e integração PostgreSQL/Redis/Flyway; evitar repetir o número histórico de 24 como cobertura atual. |
| CI | `.github/workflows/backend-integration.yml` | Secret scan, unit/build, integração sem skips, frontend quality/build, CMS build, Dependency SCA, Security E2E e Required Gate. |
| Produto | análise de produto, OKRs, roadmap e plano mensal em `docs/` | Escala mensal correta para PMEs; demais capacidades por fases e sem alegações legais não demonstradas. |
| Entrega e marketing | `docs/ferramentas-ai-product-design-go-to-market.md`, docs de entrega, AGENTS | Ferramentas assistivas, rastreabilidade, critérios AppSec e PRs protegidos. |

Os caminhos backend relativos acima estão em `Backend/java-app1/demo`; os caminhos `src/` frontend estão em `Frontend/web-app3/escala`.

## Divergências e consequências

1. AGENTS e textos históricos citam Next 16.2.6 e Framework 7.0.8; o develop remoto atualizado já contém patches posteriores. Manifests/lockfiles do commit analisado definem estado técnico. Não reverter patches para corresponder a um texto antigo.
2. O plano mensal antigo ainda fala em adotar ferramenta de migration; Flyway e migrations já existem. Novas mudanças devem usá-los, não criar controle concorrente de schema.
3. `docs/ci-gates.md` contém trecho histórico sobre inexistência de testes frontend; o workflow atual já inclui Security E2E e Dependency SCA. O plano deve preservar todos os jobs efetivos, não apenas a lista histórica.
4. A arquitetura hexagonal é direção oficial e parcialmente implementada. A constituição impede novo acoplamento, mas não demanda uma refatoração transversal na presente issue.
5. Existem dois documentos chamados ADR-009. Citar o filename completo evita confundir identidade autoritativa e erros/health.
6. Health tem subrotas de readiness/liveness na ADR específica; preservar exposição mínima de health, restringindo métricas, sem quebrar probes existentes.
7. Documentos de regras incluem capacidades futuras (IA/canais/ponto completo). Especificar estado observado e pendência; não transformar roadmap em comprovação de implementação.

## Acréscimos necessários à constituição

Além de hexagonal e fronteiras, são necessários: tenant explícito inclusive em jobs/cache, identidade autoritativa, auditoria append-only, Flyway, concorrência, contratos/comunicações, privacidade de plugins, estados UX, IDs estáveis, política de emendas e evidência real de Git/CI. São obrigações verificáveis já apoiadas nas decisões locais; não novos recursos de produto.

## Referências analisadas

- `AGENTS.md`; inventário de `docs/Arquitetura/`; `docs/Requisitos.md`; `docs/Regras-de-Negocio.md`.
- `docs/decisoes-tecnicas.md`; `docs/appsec-criterios-de-aceite.md`; `docs/arquitetura-isolamento-multi-tenant.md`; `docs/estrategia-testes-e-dados.md`; `docs/ci-gates.md`.
- ADRs de autenticação, revalidação JWT, erros/correlação/health; manifests, migrations, Compose, workflow e exemplos de código/testes indicados na tabela.
- `docs/Analise-Produto-Arquitetura-Concorrencia-Oceano-Azul.md`, `docs/okr.md`, `docs/roadmap.md`, `docs/plano-implementacao-gestao-mensal-inteligente-escalas.md`, guia de ferramentas.
- Material fornecido pelo usuário e [Spec Kit oficial](https://github.com/github/spec-kit), consultado em 2026-10-08 para estrutura e processo. O material externo não define política do Escala. Os templates desta entrega são adaptações locais; a CLI e seus comandos não estão instalados por esta issue.
