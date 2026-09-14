# Dependencias e gate SCA — issues #99 / #108

O job `Security / Dependency SCA` verifica dependencias diretas e transitivas com
Trivy. A analise inclui dependencias de desenvolvimento e bloqueia ocorrencias
High/Critical, conforme a politica AppSec. O nome do job e local ao projeto;
SCA e a pratica de analisar componentes de terceiros.

## Lockfiles oficiais

| Componente | Gerenciador e lock | Instalacao reproduzivel |
| --- | --- | --- |
| CMS `Backend/cms-strapi` | npm, `package-lock.json` | `npm ci` |
| Frontend principal `Frontend/web-app3/escala` | pnpm 10.33.3, `pnpm-lock.yaml` | `pnpm install --frozen-lockfile` |
| Frontend futuro `Frontend/web-app1/app` | npm, `package-lock.json` | `npm ci` |

Os Dockerfiles e o CI usam esses gerenciadores. O `pnpm-lock.yaml` antigo do CMS
e o `yarn.lock` antigo do frontend principal foram removidos: descreviam arvores
divergentes que nao eram usadas nesses builds. Ambos sao recuperaveis pelo Git.
O codigo do frontend futuro foi preservado e seu lock continua coberto pelo scan.

## Atualizacoes de seguranca

- CMS: Strapi e plugins alinhados em 5.53.0. Node suportado: `>=22.13.0 <23`,
  considerando tambem o requisito de `preferred-pm`/`which-pm` no lock.
- Frontend principal: Next.js 16.3.3, NextAuth 4.24.15, Axios >=1.18.0,
  Sharp 0.35.4 e Playwright 1.55.1. A imagem de E2E acompanha o Playwright.
- Frontend futuro: Next.js 15.5.24, NextAuth 4.24.15, Axios 1.18.0,
  Multer 2.3.0 e Sharp 0.35.4, preservando React 18 e a linha Next 15.
- Overrides selecionam as correcoes transitivas ainda nao resolvidas pelos pais.
  Os novos overrides por major preservam as linhas de APIs de pacotes como
  `brace-expansion`, `minimatch`, `form-data`, `js-yaml` e `protobufjs`.
- Vite 6.4.3 no CMS e uma excecao explicita ao Vite 5 fixado pelo Strapi:
  o advisory reportado nao oferece patch na linha 5. A compatibilidade deve
  continuar coberta pelo build do admin e pelo servidor `develop`.
- O override Sharp 0.35.4 cobre tambem a copia usada pelo Next/Strapi. Ao atualizar,
  validar as plataformas de deploy e a conversao de imagens/miniaturas.

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

O gate continua usando `--severity HIGH,CRITICAL --exit-code 1`, sem ignore de CVEs,
e `--include-dev-deps`. Em 2026-09-13/14, os tres locks mantidos passaram no scan
local com zero High/Critical. Isso e um resultado datado da base de advisories,
nao uma garantia de ausencia de falhas no codigo ou em imagens Docker.

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
