# Dependencias e gate SCA — issues #99 / #108

O job `Security / Dependency SCA` verifica dependencias diretas e transitivas com
Trivy. A analise inclui dependencias de desenvolvimento e bloqueia ocorrencias
High/Critical, conforme a politica AppSec. O nome do job e local ao projeto;
SCA e a pratica de analisar componentes de terceiros.

## Lockfiles oficiais

| Componente                                    | Gerenciador e lock             | Instalacao reproduzivel          |
| --------------------------------------------- | ------------------------------ | -------------------------------- |
| CMS `Backend/cms-strapi`                      | npm, `package-lock.json`       | `npm ci`                         |
| Frontend principal `Frontend/web-app3/escala` | pnpm 10.33.3, `pnpm-lock.yaml` | `pnpm install --frozen-lockfile` |

Os Dockerfiles e o CI usam esses gerenciadores. O `pnpm-lock.yaml` antigo do CMS
e o `yarn.lock` antigo do frontend principal foram removidos: descreviam arvores
divergentes que nao eram usadas nesses builds. Ambos sao recuperaveis pelo Git.
O codigo do frontend futuro `Frontend/web-app1/app` permanece preservado no
repositorio, mas nao faz parte da aplicacao atual, dos artefatos de deploy nem do
escopo SCA desta entrega. A exclusao e explicita no workflow; nao altera a pasta.

## Atualizacoes de seguranca

- CMS: Strapi e plugins alinhados em 5.56.0. Node suportado: `>=22.13.0 <23`,
  considerando tambem o requisito de `preferred-pm`/`which-pm` no lock.
- Frontend principal: Next.js 16.3.8, NextAuth 4.24.15, Axios 1.20.0,
  Sharp 0.35.5 e Playwright 1.63.0. A imagem de E2E acompanha o Playwright.
- Backend: mantidas as linhas gerenciadas pelo Spring Boot 4.1.0, com patches
  de seguranca Jackson 2.21.7/3.1.7, Netty 4.2.17.Final, PostgreSQL JDBC
  42.7.12 e Tomcat 11.0.25. Isso corrige os findings High/Critical publicados
  depois do scan verde de 2026-09-14 sem trocar majors ou contratos da aplicacao.
- Overrides selecionam as correcoes transitivas ainda nao resolvidas pelos pais.
  Os novos overrides por major preservam as linhas de APIs de pacotes como
  `brace-expansion`, `minimatch`, `form-data`, `js-yaml` e `protobufjs`.
- Vite 6.4.3 no CMS e uma excecao explicita ao Vite 5 fixado pelo Strapi:
  o advisory reportado nao oferece patch na linha 5. A compatibilidade deve
  continuar coberta pelo build do admin e pelo servidor `develop`.
- O override Sharp 0.35.5 cobre tambem a copia usada pelo Next/Strapi. Ao atualizar,
  validar as plataformas de deploy e a conversao de imagens/miniaturas.

## Aceite temporario de risco upstream

Em 2026-10-02 foi publicado o advisory `GHSA-vfj7-8cjw-p6xm` para `braces`
ate 3.0.3. Em 2026-10-03, o registry npm ainda nao oferecia versao corrigida.
O pacote e transitivo de `micromatch`/`fast-glob` e `chokidar`, usados por Strapi,
ESLint e Next Sitemap; nao e uma biblioteca chamada diretamente pelo produto.
Trocar Strapi ou o toolchain, ou usar fork nao oficial, nao e uma correcao
compativel ou proporcional.

O mantenedor aceitou temporariamente o risco para manter a aplicacao publicavel.
A excecao versionada em `.trivyignore.yaml`:

- cobre somente `CVE-2026-93687` em `braces@3.0.3`;
- limita-se aos locks do Strapi e do frontend oficial;
- expira em 2026-11-03;
- mantem os demais findings High/Critical bloqueantes;
- deve ser removida assim que `braces` ou um pacote pai publicar correcao.

Responsavel pelo acompanhamento: manutencao/engenharia do Escala, via issue #108.
Mitigacao: nao expor entrada de usuario a glob/brace expansion, manter Strapi e
Next Sitemap atualizados e revisar o advisory antes do vencimento. O aceite nao
declara a vulnerabilidade corrigida.

Reavaliar overrides nas proximas atualizacoes dos pacotes pais e remove-los quando
o lock normal resolver versoes corrigidas. Nao usar `audit fix --force` nem
alterar manualmente versoes/integridades no lock. Regenerar com o gerenciador
oficial e repetir os checks.

## Interpretacao dos logs

O Maven gera um SBOM CycloneDX com o grafo resolvido, incluindo testes. Trivy
0.74.0 ignora hashes SHA-384/SHA3 que nao reconhece, emitindo avisos, e continua
analisando os componentes por seus identificadores e versoes. Esses avisos nao
sao o motivo do exit code 1 observado no run #86. Nesse run, o scan Maven passou;
as arvores JavaScript somaram 350 ocorrencias High/Critical (nao vulnerabilidades
unicas, pois havia duplicacao entre locks).

O `CI / Required Gate` propaga o resultado dos jobs obrigatorios; ele tambem
falha se qualquer um deles for cancelado. Um novo push pode cancelar a execucao
anterior pela configuracao de concorrencia do workflow.

## Verificacao e rollback

O gate continua usando `--severity HIGH,CRITICAL --exit-code 1` e
`--include-dev-deps`, com a unica excecao descrita acima. Em 2026-09-13/14, os locks mantidos passaram no scan
local com zero High/Critical. Isso e um resultado datado da base de advisories,
nao uma garantia de ausencia de falhas no codigo ou em imagens Docker.
Em 2026-10-03, a base atual voltou a reportar High por causa do advisory de
`braces` sem patch. O resultado deve continuar mostrando o finding como
suprimido, permitindo o gate apenas enquanto a excecao estiver valida.
Em 2026-10-04, o SBOM Java foi refeito com os patches acima e passou no Trivy
0.74.0 com zero findings High/Critical corrigiveis; a suite Maven passou com
122 testes.

Validacoes aplicaveis: instalacao limpa, lint/typecheck/build do frontend
principal, suite E2E Auth/BFF/tenant, testes e build do Strapi, startup com
PostgreSQL de teste, admin/publicacao editorial e processamento de imagens.
O CI do commit do PR e a evidencia final para os gates obrigatorios.

Rollback deve reverter manifesto e lock juntos, alem da imagem Playwright quando
aplicavel. A reversao reintroduz os advisories conhecidos e exige novo scan e
tratamento de risco antes de qualquer release. Uma atualizacao do Strapi sobre
banco existente exige backup/restauracao compativel; o smoke local usa banco
descartavel e nao comprova rollback de dados de producao.

Referencias: [politica AppSec](appsec-criterios-de-aceite.md),
[GitHub supply chain security](https://docs.github.com/en/code-security/concepts/supply-chain-security/supply-chain-security),
[leitura de hashes do Trivy 0.74.0](https://github.com/aquasecurity/trivy/blob/v0.74.0/pkg/sbom/cyclonedx/unmarshal.go).

## Atualização da linha Spring Framework — issue #118

Em 06/10/2026, o CI de #117 detectou CVE-2026-47884 em spring-webmvc 7.0.8.
A remediation #108 já estava em develop, mas não atualizava Spring Framework.
O POM passa a definir spring-framework.version=7.0.9, alinhando todos os
módulos pelo BOM gerenciado sem mudar Spring Boot 4.1.0 ou Java 25.

O [advisory oficial](https://spring.io/security/cve-2026-47884/) confirma o
patch OSS 7.0.9 e condiciona exploração a XsltView com mapping /** e resolução
implícita de view. Nenhum uso dessa configuração foi encontrado no código
inspecionado; isso não equivale a comprovação de ausência em todo runtime.
O scanner classifica o finding como Critical, enquanto Spring o classifica
como Medium. O gate do projeto continua obrigatório e não recebe supressão.

A evidência de saída exige dependency tree/SBOM sem módulos Framework 7.0.8,
SCA sem High/Critical não aceitos, testes/build, integração e Docker com
health/Swagger/OpenAPI. Consultar os checks do PR relacionado à issue #118
para os resultados efetivamente executados; não presumir aprovação por este
registro de estratégia. Não há alteração REST, tenant ou regra de negócio.

Rollback técnico: reverter a propriedade e usar o digest anterior, sem migration
de banco. Isso reintroduz a dependência afetada e bloqueia release até tratamento
de risco; não é uma alternativa produtiva aprovada.

No primeiro CI desta correção, o SBOM Java passou com zero High/Critical.
O scan então avançou para JavaScript e detectou CVE-2026-93749 HIGH em
source-map-js 1.2.1. O pacote estava nos locks do CMS e frontend, via PostCSS
e tooling de CSS. A atualização complementar fixa o override transitivo em
1.2.2 nos dois manifests e regenera seus locks, sem dependência direta nova.
O [advisory revisado](https://github.com/advisories/GHSA-68fv-2mgg-jv7q)
indica esse patch para negação de serviço com offsets inválidos em source maps
indexados. Não foi encontrado consumo direto desse pacote no código da
aplicação. Builds e checks de frontend/CMS validam os consumidores existentes;
o SCA completo continua sendo condição de integração, sem nova exceção.
