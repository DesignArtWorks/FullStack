# Evidências SCA — 16/11/2026

RN-SCA99-001/002; FR-001/002; T001–T003. Base 28488b6575c4260204c9ecef1fbda1cbb7db2850.

## Registry e advisories

- `npm view braces version`: 3.0.3.
- `npm view zod version`: 4.6.7; `npm view 'zod@3' version --json`: última 3.25.76.
- `npm view koa-session version dependencies --json`: 7.0.2, zod ^3.24.1.
- [Braces GHSA-vfj7-8cjw-p6xm](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm): retirado em 23/10/2026, sem versão patched indicada.
- [Zod GHSA-mrhx-c7xc-m3w5](https://github.com/advisories/GHSA-mrhx-c7xc-m3w5): página consultada ainda apresenta High, afetados <4.6.6; [NVD CVE-2023-54404](https://nvd.nist.gov/vuln/detail/CVE-2023-54404) registra rejeição. Divergência explícita; Trivy atual não reporta esse ID sem supressões.

Conclusão limitada: nenhum upgrade necessário para resolver o SCA atual. Não forçar API Zod 4 no consumidor 3 nem fabricar uma versão de braces. Remover as duas exceções fortalece o gate caso a base volte a reportar findings.

## Execução

Imagem exata do CI: `aquasec/trivy:0.74.0@sha256:62b1e65e8869bc4b4c6aa4fa2b21595256c7c2f6018a9d9ad61caf87187c1969`.
DB schema 2: UpdatedAt `2026-11-16T13:03:44.830426244Z`; NextUpdate `2026-11-17T13:03:44.830426014Z`.

SBOM no backend:

```sh
mvn -B --no-transfer-progress org.cyclonedx:cyclonedx-maven-plugin:2.9.1:makeAggregateBom -DoutputFormat=json -DincludeTestScope=true -DoutputDirectory=/tmp/escala-99-sca/maven
```

PASS, 173 componentes. Avisos de metadata 401 em dois repositórios opcionais e hashes não suportados não impediram resolução/análise. Scanner identificou 174 pacotes Java incluindo a raiz.

Comandos Trivy executados em container com repo read-only e cache/relatórios temporários, sem secrets scanner:

```sh
trivy sbom --cache-dir /work/cache --severity HIGH,CRITICAL --exit-code 1 --ignorefile /dev/null --format json --output /work/reports/maven.json /work/maven/bom.json
trivy fs --cache-dir /work/cache --scanners vuln --offline-scan --severity HIGH,CRITICAL --exit-code 1 --ignorefile /dev/null --include-dev-deps --skip-dirs '**/node_modules' --format json --output /work/reports/cms-strapi.json /repo/Backend/cms-strapi
trivy fs --cache-dir /work/cache --scanners vuln --offline-scan --severity HIGH,CRITICAL --exit-code 1 --ignorefile /dev/null --include-dev-deps --skip-dirs '**/node_modules' --format json --output /work/reports/escala.json /repo/Frontend/web-app3/escala
```

| Alvo | Pacotes identificados | High/Critical | Exit |
| --- | --- | --- | --- |
| Maven SBOM | 174 (inclui raiz) | 0 | 0 |
| CMS package-lock.json | 1355 | 0 | 0 |
| Frontend pnpm-lock.yaml | 740 | 0 | 0 |

Primeira tentativa CMS/frontend falhou antes da análise por escrita negada no cache do bind mount. Permissões ajustadas somente no diretório temporário; repetição real acima passou. Erro de execução não foi tratado como finding nem PASS.

Relatórios locais: `/tmp/escala-99-sca/reports/`. Não são artefatos de CI remoto nem prova de runtime seguro. Reexecução com política vazia versionada e validação documental serão registradas em tasks.md.

## Pendências independentes

#106: AB-001/003/004, contrato US2, rotação e governança. Consulta GET do repositório não retornou `security_and_analysis`; ausência do campo não comprova push protection ligada ou desligada. #109: alvo implantado/ponto externo não identificados. Perguntas ao mantenedor nesta conversa ainda pendentes. Gate final #99 não executado. Canais Slack identificados por histórico: escala-dev e escala-incidentes; comunicação final ainda não enviada.
