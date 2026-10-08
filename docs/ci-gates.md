# CI obrigatorio do monorepo

## Objetivo

O workflow `.github/workflows/backend-integration.yml` e o gate tecnico de Pull Requests destinados a `develop` e `main`. Ele tambem executa em pushes para `main` e pode ser disparado manualmente para diagnostico. Pushes em `develop` nao iniciam uma segunda execucao: essa branch so recebe mudancas por Pull Request ja validado, e a promocao para `main` abre outro Pull Request. O caminho historico foi preservado para que o proprio Pull Request de bootstrap execute o workflow ja reconhecido pelo GitHub.

O workflow nao usa filtros por caminho: uma mudanca transversal sempre executa todos os checks obrigatorios. O `GITHUB_TOKEN` possui somente `contents: read`, e nenhuma validacao basica depende de secrets de producao.

A organizacao restringe o GitHub Actions a workflows e actions locais. Por isso o pipeline nao depende de actions externas: usa Git nativo para checkout e imagens oficiais Docker para Maven/Java 25 e Node.js 22. Essa decisao preserva a politica organizacional sem conceder uma excecao ampla a terceiros.

## Checks estaveis

| Check | Comandos e garantias |
| --- | --- |
| `Backend / Unit and Build` | uma unica execucao `mvn -B package`, que compila, executa os testes unitarios e gera o artefato em `maven:3.9-eclipse-temurin-25` |
| `Backend / Integration` | exige `docker info`, executa `mvn -B -Pintegration test` em Java 25 com acesso ao Docker/Testcontainers e rejeita relatorios ausentes, vazios ou com testes ignorados |
| `Frontend / Quality and Build` | `pnpm install --frozen-lockfile`, `pnpm run lint`, `pnpm run typecheck` e `pnpm run build` |
| `CMS / Build` | `npm ci`, `npm run test:security` e `npm run build` no Strapi oficial |
| `Security / Versioned Secret Scan` | PowerShell rastreado, controles sintéticos e Gitleaks histórico completo de develop/head PR; saída 42 bloqueia por finding, 2 por erro |
| `Security / Dependency SCA` | CycloneDX/Trivy bloqueiam vulnerabilidades High/Critical não aceitas |
| `Frontend / Security E2E` | Playwright Auth/BFF/Tenant com backend simulado; não substitui integração Spring |
| `CI / Required Gate` | falha se qualquer check anterior falhar, for cancelado ou ignorado |

O perfil Maven `integration` inclui as classes em `src/test/java/**/integration`. Elas usam Testcontainers com `disabledWithoutDocker = false`, PostgreSQL real, Redis real e Flyway. O passo posterior aos testes inspeciona os relatorios Surefire e impede sucesso quando a suite de integracao nao executa de verdade.

A suíte de segurança frontend executa em `Frontend / Security E2E`. Lint/typecheck/build permanecem no job de qualidade. O scanner histórico e suas limitações estão em [secret-scanning-issue-106.md](secret-scanning-issue-106.md).

## Branch protection

O nome a cadastrar como required status check nos rulesets de `develop` e `main` e:

```text
CI / Required Gate
```

Os sete checks detalhados permanecem visiveis para diagnostico, enquanto o gate agregado oferece um nome unico e estavel para a protecao. A configuracao do ruleset e a comprovacao de merge bloqueado pertencem a issue #41 e devem ser aplicadas somente depois que este workflow produzir ao menos uma execucao verde no GitHub.

## Diagnostico

- `startup_failure` antes da criacao dos jobs: confirmar que o workflow nao referencia actions externas; a organizacao permite somente actions e workflows locais.
- Falha antes dos testes de integracao em `docker info`: o runner nao oferece Docker; o job deve permanecer vermelho.
- Relatorio de integracao ausente ou com `Skipped`: investigar Testcontainers; nao remover a verificacao para obter verde.
- Falha Flyway/Hibernate: tratar a migration ou incompatibilidade de schema em issue propria.
- Falha frontend: executar localmente lint, typecheck e build com o lockfile atual.
- Falha de cache: caches aceleram a instalacao, mas os comandos continuam usando os lockfiles como fonte deterministica.

Os relatorios Surefire permanecem no workspace durante o job e seus resumos aparecem nos logs Maven. Logs nao devem conter credenciais reais.

## Responsabilidade unica por ambiente

- CI valida e gera artefatos; nao inicia a stack de desenvolvimento.
- Dockerfiles instalam dependencias e geram os artefatos das respectivas imagens.
- O Compose raiz somente orquestra imagens, volumes, configuracao e healthchecks.
- Restart de containers somente reinicia processos; instalacoes ocorrem em rebuilds.

O inventario e as evidencias da consolidacao estao em
`docs/auditoria-pipelines-issue-47.md`.

## Rollback

Se um erro do workflow bloquear merges, investigar o job, corrigir em branch dedicada e validar via PR ou workflow_dispatch. Registrar causa e recuperação. Não remover required checks, transformar falhas em skips ou usar bypass para obter integração.
