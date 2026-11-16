# Plano — #99 SCA

Branch: `security/issue-99-sca-revalidation`. Base `origin/develop`: `28488b6575c4260204c9ecef1fbda1cbb7db2850`, fetch em 16/11/2026. Constituição 1.0.0.

## Gate e fases

[Dossiê](interview.md): P01 autoriza revalidação e patch compatível. H1 reutilizado desse pedido concreto; não há nova regra operacional. H2 após resultado/diff; revisão humana antes da integração. Decisões #106/#109 bloqueiam apenas seus trechos dependentes.

## Contexto e arquivos previstos

Spring Boot 4.1.0/Java 25; CMS npm/Node22; frontend pnpm. Trivy 0.74.0 com digest do workflow existente. Domínio/portas/adapters não são alterados; modularização legada permanece.

- Reusar `.github/workflows/backend-integration.yml`, manifests/locks oficiais e testes existentes para baseline.
- Alterar `.trivyignore.yaml` somente quando evidência permitir retirar exceção; não ampliar supressões.
- Manifests/locks CMS/frontend ou POM somente se SCA exigir patch compatível, seguido de build/testes do consumidor.
- Atualizar `docs/dependencias-sca-issue-108.md` e esta spec com comandos/resultados reais.

## Constitution Check inicial

| Check | Desenho/evidência | Resultado |
| --- | --- | --- |
| CON-01 | Três componentes oficiais; CMS editorial | PASS |
| CON-02 | Nenhuma lógica de negócio ou migração arquitetural | N/A runtime neste diagnóstico |
| CON-03 | Nenhuma identidade/tenant/credencial testada | N/A autorização runtime |
| CON-04 | Sem nova regra de escala; abertas #106 separadas | PASS recorte SCA |
| CON-05 | Relatórios de dependências sem secrets; mounts de leitura | PASS desenho |
| CON-06 | Nenhuma alteração contratual; Slack só metadados sanitizados | PASS desenho |
| CON-07 | Sem alteração visual ou claim de conformidade | N/A UI |
| CON-08 | RN/FR ligados a T001–T004 e scanner real | PENDING execução |
| CON-09 | Branch dedicada após fetch, checkout inicial limpo | PASS preparação; PR/revisão pendentes |

## Validação e rollback

Gerar SBOM CycloneDX com dependências de teste; Trivy sobre SBOM e locks com HIGH/CRITICAL, dev incluído e `/dev/null` como ignorefile na baseline. Artefatos temporários em `/tmp/escala-99-sca`. Mudança apenas documental/política exige diff/links e repetir SCA; alterações runtime exigem testes/build e gates específicos. Não reexecutar todos os builds por alteração exclusivamente documental.

Rollback por PR coeso; não restaurar supressão vencida como aprovação de risco. Não alterar proteção, promover main, rotacionar credencial não mapeada ou declarar deploy.

## Constitution Check pré-PR

Revisão em 16/11/2026, antes de PR. Evidências em research.md e tasks.md.

| Check | Resultado e evidência |
| --- | --- |
| CON-01 | PASS: escopo SCA nos três componentes oficiais, sem mudança de fronteiras |
| CON-02 | N/A runtime: nenhuma alteração de domínio/adapter/dependência; migração hexagonal não alegada |
| CON-03 | N/A runtime: nenhuma credencial, autorização ou dado de tenant alterado |
| CON-04 | PASS recorte: política estrita preservada; decisões #106 pendentes sem implementar trecho dependente |
| CON-05 | PASS: relatórios de dependências locais; nenhum segredo coletado/publicado; env local preservado |
| CON-06 | PASS: nenhum contrato REST/BFF alterado; Slack final ainda pendente, somente metadados |
| CON-07 | N/A UI; nenhum claim de segurança universal/produção liberada |
| CON-08 | PASS local: SCA três componentes zero High/Critical, repetição da política PASS, diff/links PASS; CI remoto PENDING |
| CON-09 | PASS preparação: branch dedicada/base atual; diff coeso sem .gitignore concorrente; PR/revisão/merge PENDING |

Este check permite PR de revisão, não integração nem fechamento #99. H2 permanece com Wemerson. Retirar exceções não é aceitar risco novo; se a base voltar a reportar findings, o gate os bloqueia.
