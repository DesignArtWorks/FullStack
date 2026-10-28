# Plano — SCA #99

Branch security/issue-99-sca-dependencies, base origin/develop fa9c27e33d7195928a65ba62540753ec0310d077 após fetch2026-10-27. Worktree separado preserva PR129 e UI suja. Constituição1.0.0; H1 aprovado PROV-SCA002. H2 antes de integração desta fatia.

## Desenho e arquivos

CMS package.json/package-lock: DOMPurify3.4.17, MCPserver2.3.1, follow-redirects1.16.2, markdown-it14.3.3, undici6.29.1, Vite6.4.4, Zod4.6.7 com seletor restrito ao major4. Zod3 fica compatível e explicitamente pendente. Sem upgrade geral do Strapi.

tests/cms-dependency-security.test.mjs: gatilho ShadowDOM/IN_PLACE, array bounded Zod4 e controles de schema/Markdown/koa. Nova devDependency jsdom27.4.0 justificada: reproduzir sanitização DOM real sem avaliar payload JS, compatível Node>=22.13 vigente; não vira dependência operacional. JobCMS instala --include=dev para executar essa verificação; nenhum gate removido. Specs desta pasta; frontend manifests/lock apenas se scanner comprovar achados ali.

Fluxo: dependências CMS → sanitizador/editor/parser/HTTP → conteúdo editorial. Identidade operacional permanece Spring/BFF. Investigar Zod3 por fonte, sem presumir mitigação; nova aceitação exige Wemerson/segurança com escopo/prazo.

## Constitution Check antes de tarefas

| CON | Estado/desenho |
| --- | --- |
| 01 | PASS — CMS editorial; nenhuma mudança Spring/BFF |
| 02 | PASS desenho — patches + dependência exclusivamente teste justificada |
| 03 | PASS desenho — sem tenant/identidade nova; controle Strapi real |
| 04 | N/A — sem regra escala |
| 05 | PASS desenho — fixtures sintéticas, banco novo, zero migration/secret |
| 06 | PASS desenho — sem contrato novo; controles consumidores obrigatórios |
| 07 | N/A — sem UI/claim |
| 08 | PENDING — RED/GREEN, build e SCA ainda executar; remanescente não é PASS |
| 09 | PENDING — branch/H1 corretos, H2/PR/checks/merge ainda pendentes |

## Investigação independente

Fontes: koa-body6/src/index.ts e types.ts; koa-session7/src/index.ts; Strapi5.56 body.ts/session.ts; middlewares.ts do repositório. BodyConfig é estático, mas koaBody(options) é invocado por request no Strapi. Seu Zod valida opções, não o body HTTP; array conhecido é parsedMethods. Session valida opções sem array explícito. Não demonstrado input HTTP→sink array vulnerável nesses dois consumidores; não equivale a CVE corrigida nem autoriza waiver. Zodv3 não terá novas releases; v3 embutido em4.6.7 não ganhou correção. Fontes upstream: https://github.com/colinhacks/zod/issues/6653 e https://github.com/colinhacks/zod/pull/6652. Patchv4 corrige somente coleções limitadas antes do walk; arrays ilimitados permanecem responsabilidade do consumidor.

## Validação e rollback

RED focal em libs antigas quando viável; GREEN após lock novo; npm ci determinístico com Node22, testes, TypeScript/build, Strapi/PostgreSQL descartáveis e controles do PR129. SCA percorre CMS e frontend separadamente, mesmo se primeiro falhar, para não ocultar segunda superfície. Lint CMS não tem script; build inclui TS. Docker/health/Swagger Spring se código operacional mudar; não usar imagem antiga como prova atual. Antes de PR repetir CON-01/09 com resultados reais.

Rollback: reverter commit de pins/lock/testes/CI, mantendo bloqueio de identidade. Reintroduz versões vulneráveis e não autoriza produção. Não mexer em .trivyignore.yaml sem decisão formal, scanner/threshold/permissões inalterados.

## Retomada em 2026-10-28

origin/develop permanece fa9c27e após novo fetch. Relatório independente do frontend no mesmo Trivy: 6 ocorrências (follow-redirects1.16.0 duas High, Next16.3.8 duas Critical/uma High, Zod4.1.12 High). Complemento necessário ao FR-001 já aprovado: Next/eslint-config-next16.4.1, Zod4.6.7 e follow-redirects1.16.2; nenhum código UI/contrato alterado. Scanner CMS após pins: novos módulos resolvidos deixam MCPnode2.0.0 High (GHSA-f897-62hf-6gxq) e Zod3.25.76 High; adicionar MCPnode2.1.1 da mesma linha suportada. Sem novo ignore.

Dois testes DOM foram corrigidos: SAFE_FOR_TEMPLATES pode remover texto adjacente à expressão no mesmo nó; o controle editorial passou para elemento span separado. Não houve enfraquecimento da negativa. RED repetido nas libs antigas; GREEN local 22/22 com zero skips na primeira passada corrigida. Build ainda em andamento; reinstalação final inclui novo pin MCPnode.

Investigação fresca frontend confirmou sharp>=0.35.5 no lock, images.unoptimized e proxy bloqueando /_next/image; não demonstrada exploração das falhas listadas. Não encontrado use cache/cacheComponents no recorte inspecionado; não é prova universal. Advisories oficiais exigem Next16.4.1: https://github.com/advisories/GHSA-2464-fhr4-w9xw e https://github.com/advisories/GHSA-673q-mjm6-r5xr. Preservar esses controles e exercitar lint/typecheck/build/E2E/produção com runtime atualizado.

## Evidência de validação local — 2026-10-28

- CMS: instalação limpa npm ci --include=dev no Node22.23.2; suite final 23/23, zero skips, incluindo middleware real com arrays HTTP versus opções estáticas; TypeScript/admin build aprovado após pins finais (suite anterior22/22).
- Strapi real + PostgreSQL exclusivo descartável: harness da #103/PR129 executado contra dependências deste candidato, 5/5 TAP (4 cenários): JWT/IDs coincidentes rejeitados; admin, API token, publicação e leitura editorial preservados.
- Frontend: instalação frozen, lint sem erros (77 warnings preexistentes), typecheck e build Next16.4.1 aprovados. Nenhuma alteração UI ou contrato.
- Produção: mesmo build, next start --hostname localhost, DNS IPv4 e requests localhost: health/home/login200, dashboard307login, BFFusers/me401, image404. Strapi propositalmente indisponível exercitou fallback, não valida integração editorial frontend. Bind127.0.0.1 gerou307 autorreferente em home/login com rewrite absoluto localhost; trocar somente a URL cliente não resolveu, bindlocalhost resolveu. Docker oficial usa pnpm start sem hostname explícito; deploy real/Compose não validado por este smoke. Preservar evidência de falha, não declarar produção liberada. Fonte do mecanismo: https://github.com/vercel/next.js/issues/94342 . Sem patch especulativo em proxy/next-intl.
- Trivy0.74.0 no digest do CI, vulnerabilidades incluindo dev e ignore existente: frontend exit0, zero High/Critical não aceitos; CMS exit1, resta Zod3.25.76 / CVE-2023-54404 High. MCPnode adicional corrigido2.1.1. Exceção preexistente braces3.0.3 com expiração2026-11-03 permanece intocada; zero não aceitos não significa zero findings absolutos.
- Zod3: análise independente dos consumidores + teste de runtime demonstra apenas parsedMethods estático atingindo ZodArray em dois requests; não demonstra remediação da biblioteca nem segurança de todos os usos futuros. koa-session7 depende da API v3 de function().args().returns(); override major4 ou alias zod/v3 em4 não é correção compatível. Sem exceção nova; aceite de risco delimitado exige decisão humana com responsável, mitigação e prazo.
- Downloads verificados com CAs públicas de C:\certs e trust store da estação montados temporariamente. Sem desativar TLS, sem CA/credenciais no repositório. Dados de teste sintéticos.
- Playwright e revisão independente do candidato em andamento. H2 desta fatia permanece pendente. CON-08 bloqueado pelo SCA CMS; CON-09 parcial (branch/H1/spec corretos, sem merge). Demais CON sem mudança de escopo.

Complemento E2E: primeira suíte Playwright1.63/Chromium:5pass/1fail, login UI excedeu30s e retry autenticou mas permaneceu /en/login em vez de dashboard. Reabertura do cache gerou panic Turbopack stale TaskId; preservado .next/dev em diretório irmão e repetido cache novo, falha de navegação reapareceu. Sem relaxar assertivas, sem classificar como mero problema de ambiente. Suite E2E NÃO aprovada; pendência adicional a investigar/confirmar em runner Linux do CI. Os dois cenários de avatar passaram na primeira execução. Controle de segredos versionados e git diff --check aprovados. Nenhuma promoção autorizada com estas pendências.

Reprodução final com cache novo:4 casos de segurança,2fail/1flaky/1pass. Falhas no helper de login/navegação, sem evidência de vazamento cross-tenant; controles de tenant não podem ser considerados validados nessa execução porque o login falhou antes. PR draft130 publicado em https://github.com/DesignArtWorks/FullStack/pull/130, head inicial b77310a; CI129/run40704503495 iniciado. Nenhum merge/waiver/H2 solicitado com pendências conhecidas. git diff origin/develop --check final aprovado após normalização da linha vazia de tasks.md; controle de segredos passou. Estimativa preliminar comunicada ao responsável:1–3dias de engenharia para adaptar/substituir consumidores Zod3 e validar sessões/body/uploads/admin; não é orçamento fechado nem autorização de fork. Mantida investigação no escopo H1 vigente.
