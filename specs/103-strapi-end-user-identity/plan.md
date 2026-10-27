# Plano #103

Branch: security/issue-103-strapi-end-user-identity. Base: origin/develop fa9c27e33d7195928a65ba62540753ec0310d077; fetch em 2026-10-27. Worktree isolado preserva UI local. Constituição 1.0.0.

## Gate e fases

Dossiê interview.md revisado; H1 aprovado por Wemerson nesta conversa, escopo exato PROV-103-004. Não simular entrevista adicional. H2 PENDING após MVP; nenhuma autorização de merge/main inferida.

Setup: inventário, origem do patch e testes existentes. Foundational: banco e processo descartáveis. US1/MVP: suíte real HTTP/persistência/admin/editorial. Polish: diff, testes/build e evidências; apresentar para H2.

## Desenho/arquivos

Reutilizar Strapi5.56.0, Node22, pg e node:test existentes; sem dependência nova. tests/integration/end-user-identity.test.cjs inicia processo real, fixtures e HTTP. package.json expõe comando separado sem skip. CI CMS executa integração isolada se cabe no gate aprovado. src/extensions/users-permissions/strapi-server.ts só muda após falha comprovada. specs/103-strapi-end-user-identity contém rastreabilidade e evidências. Não alterar Spring/frontend.

## Constitution Check antes de implementar

| Princípio | Evidência/desenho | Estado |
| --- | --- | --- |
| CON-01 | CMS editorial; identidade Spring/BFF | PASS |
| CON-02 | Sem regra de negócio/framework no domínio | PASS |
| CON-03 | JWT legado negado; controles admin/API token distintos | PENDING execução |
| CON-04 | Sem regra de escala alterada | N/A |
| CON-05 | Dados sintéticos e banco descartável; sem alteração de schema/dado real | PASS desenho |
| CON-06 | Contrato removido já existe; verificar 404/401/403 e consumidores Spring/BFF | PENDING execução |
| CON-07 | Sem UI/claims publicados | N/A |
| CON-08 | RN → FR → tarefas → testes; resultados reais obrigatórios | PENDING execução |
| CON-09 | Worktree/base atual dedicada; H1 aprovado; H2/PR/checks/integração pendentes | PENDING entrega |

Pendências de execução não autorizam afirmar fechamento. Dono: Codex para testes; Wemerson para H2/governança. Repetir check antes de PR.

## Validação e rollback

Executar node:test focal; suíte security; typecheck/build CMS; suíte HTTP real com PostgreSQL novo. Validar backend oficial Docker/health/Swagger/OpenAPI se código backend mudar; registrar limites se não rodar. Revisão independente de bypass/regressão após foco passar. Não expandir para SCA/scan global de #99 sem evidência/escopo. Rollback dos testes/configuração: reverter commit desta fatia; nunca reativar auth legado ou apagar dados reais. Containers de teste removidos ao final por nomes explícitos, sem volumes de produção.

## Execução

Antes da mudança: node --experimental-strip-types --test tests/end-user-identity.test.mjs tests/database-credentials.test.mjs em Node24.19.0: 7 PASS, zero skips. Não é validação completa nem Node22 suportado. Imagem local cms-strapi:develop: Strapi5.56.0, pg8.8.0, Node22.23.2; lock atual precisa conferir para build final.

## Investigação e revisão do candidato (2026-10-27)

Fonte: patch existente 1bb8d90, extensão users-permissions/strapi-server.ts, rotas user-account vazias e ausência de user-theme. A estratégia instalada do plugin chama jwt.getToken antes de fetchAuthenticatedUser(id); a extensão recusa Authorization e issue/verify. A estratégia api-token é independente; os controles reais exercitam essa distinção. Tema vigente: BFF users/[id]/theme → Spring UserManagementController → UserManagementService.updateTheme com autorização por alvo. Não há alteração de contrato Spring.

A investigação delegada não conseguiu ler arquivos: ferramenta ficou sem resposta e foi interrompida, sem evidência independente. O agente principal realizou a passagem separada pela fronteira, chamadores e persistência. Revisão fresca de bypass/regressão recebeu somente candidato/escopo/política, sem alegação de testes verdes: nenhum defeito concreto identificado; limitada à descrição fornecida, sem inspeção independente do repositório. Não apresentar essa revisão como scan global ou auditoria independente de fonte.

Primeira execução com dependências antigas da imagem: 9/13 PASS, quatro FAIL Handlebars preexistentes; imagem não representa o lock atual. Instalação limpa corrigiu o ambiente: 13/13 PASS, build/compilação TS/admin PASS em Node22.23.2 Alpine; primeira suíte real falhou por esperar 404 e receber 405 em rota removida. Expectativa corrigida com base no catch-all estático, sem alterar autenticação; segunda execução: quatro cenários + teste pai PASS, zero skips. Rejeição de JWT válido 401, mutação anônima 403, admin login200/create201, API token publish201 e leitura pública200; snapshots e contagens inalterados após negativas. Runtime: no_change; gatilho original não reproduz no recorte exercitado.

Actionlint do workflow e node --check da suíte: PASS. Git diff --check: PASS. CMS não possui script lint separado; build inclui verificação TypeScript. Código Spring/frontend preservado: testes/health/Swagger Spring não foram reexecutados neste recorte de cobertura CMS, não usados como prova atual. CI obrigatório remoto, SCA global/scan final #99, H2 e integração continuam PENDING. Nenhum gate enfraquecido, secret real ou certificado adicionado ao repositório.

Reprodução e requisitos do banco: Backend/cms-strapi/tests/integration/README.md. A validação final adicional em Node22 Debian do CI está em execução; registrar seu resultado antes do PR. Certificado isolado C:/certs/kaspersky-ca-current.pem falhou TLS; bundle temporário C:/certs + cadeia pública confiável Windows teve HTTPS verificado200. CA somente em download, sem TLS desabilitado/alteração de C:/certs ou configuração versionada.

## Fechamento do MVP antes de PR — 2026-10-27

Validação final em node:22-bookworm-slim (Node22.23.2, npm10.9.8) e PostgreSQL16 novo: npm ci PASS; npm run test:security 13/13 PASS; npm run build PASS (TypeScript e painel admin); npm run test:security:integration 5/5 TAP PASS (quatro cenários mais teste pai), zero skips/falhas. Banco strapi_final_identity_test em container isolado, sem acesso ao banco operacional. O bundle externo temporário manteve TLS verificado nos downloads; nenhum certificado/configuração local incorporado ao diff. Gate scripts/security/check-versioned-secrets.ps1 PASS; node --check PASS; actionlint PASS; git diff --cached --check PASS.

| Constituição — reavaliação antes de PR | Estado e evidência |
| --- | --- |
| CON-01 | PASS — nenhum runtime alterado; CMS editorial e identidade Spring/BFF preservados |
| CON-02 | PASS — somente testes/configuração/docs, sem nova dependência ou acoplamento de domínio |
| CON-03 | PASS no recorte — IDs coincidentes/JWT válido rejeitados, snapshots intactos; sem recurso operacional tenant-bound alterado |
| CON-04 | N/A — nenhuma invariante de escala alterada |
| CON-05 | PASS — fixtures sintéticas, banco descartável, zero migration/dado real/secret versionado |
| CON-06 | PASS no recorte — 404/405 nas removidas, 401/403 em identidade final, controles admin/editorial200/201; Spring/OpenAPI não alterados |
| CON-07 | N/A — sem alteração UI ou claim comercial |
| CON-08 | PASS local — RN-SEC103-001/003 nos testes, spec/tasks, comandos/resultados acima; CI remoto e gate global não substituídos por estes resultados |
| CON-09 | PENDING — branch/base/H1 corretos; commit/PR para revisão, checks remotos/H2/integração não são presumidos |

Resultado da fronteira: no_change no runtime. Testes reais mostram que o caminho reportado não reproduz nesta develop; administração e publicação/leitura legítimas preservadas. Risco residual: cobertura focal não prova todos os cenários de produção, não resolve demais filhos nem o scan final #99. Rollback: reverter somente commit dos testes/configuração/docs, preservando bloqueio de identidade já existente. H2 requer revisão humana deste diff e evidências antes de fase posterior/merge. As issues #103/#99 permanecem abertas. O SHA real desta entrega será registrado no PR e no resumo da conversa; não inventar auto-referência no próprio commit.
