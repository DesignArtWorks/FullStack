# Correção incremental do Handlebars no CMS — #108 / #99-F

## Enquadramento e proveniência

- [FATO] O SCA do PR #126, execução `37836456753`, bloqueou Handlebars 4.7.9 por CVE-2026-106445 e CVE-2026-106446; a falha do Required Gate é consequência desse bloqueio.
- [EVIDÊNCIA] Os avisos oficiais [GHSA-p8wg-vrv2-v86f](https://github.com/handlebars-lang/handlebars.js/security/advisories/GHSA-p8wg-vrv2-v86f) e [GHSA-8r5x-fm3f-whwj](https://github.com/handlebars-lang/handlebars.js/security/advisories/GHSA-8r5x-fm3f-whwj) indicam 4.7.10 como versão corrigida.
- [DECISÃO] Wemerson aprovou H1 em 2026-10-08: atualizar somente Handlebars transitivo de 4.7.9 para 4.7.10, ajustar manifest/lockfile, testes e documentação necessários, validar CMS e todos os checks, sem ignorar CVEs nem alterar proteções.
- [FATO] O consumidor é `@strapi/generators@5.56.0`, diretamente e por plop/node-plop. O Docker de produção copia node_modules completo.
- [ABERTA] Não foi demonstrada acessibilidade desses cenários a um atacante no CMS implantado. Isso não elimina o bloqueio SCA da dependência.

As três canônicas: corrigir a dependência transitiva do CMS; para mantenedores e usuários do Escala; sucesso testável por regressões que falham em 4.7.9 e passam em 4.7.10, geração legítima preservada, build do CMS e todos os checks obrigatórios verdes no commit do PR. O escopo técnico foi explicitamente revisado pelo usuário; não há decisão de negócio nova a inferir.

## Desenho e escopo

Constitution Check de desenho consolidado nesta fase (validações ainda pendentes):

| Princípio | Aplicação/desenho | Estado e responsável |
| --- | --- | --- |
| CON-01 | Correção no contexto content/CMS, sem alterar fluxo BFF/Spring/PostgreSQL | PASS, agente |
| CON-02 | Override existente; nenhum acoplamento, framework ou dependência direta nova | PASS, agente |
| CON-03 | Nenhuma alteração de identidade, autorização ou recurso tenant-bound | N/A justificado, agente |
| CON-04 | Nenhuma regra operacional ou decisão de negócio nova | N/A justificado, agente |
| CON-05 | Dados fictícios nos testes; nenhuma credencial, migration ou alteração de banco | PASS no desenho, agente |
| CON-06 | Nenhuma alteração REST/BFF/eventos/comunicações | N/A justificado, agente |
| CON-07 | Nenhuma mudança visual ou claim de conformidade | N/A justificado, agente |
| CON-08 | IDs RN-SCA108-HB001–003, RED antes do patch e GREEN/build/SCA obrigatórios | PENDING validação, agente |
| CON-09 | Branch dedicada/base registrada, H1 aprovado; PR/checks/H2 ainda não executados | PENDING entrega e revisão, agente/Wemerson |

Este registro consolida o desenho na fase em curso; o H1 antecedeu a alteração do manifest. Não inventa aprovação de CI ou merge.

Branch dedicada `security/issue-108-handlebars-patch`, baseada em develop `0e9e58dfbdeb46c3d99107739566d9dd10dd62da`. Adicionar override exato `handlebars: 4.7.10`, seguindo o mecanismo já usado no CMS. Não adicionar dependência direta nem atualizar Strapi, backend, frontend, banco ou contratos REST. Arquivos: package.json, package-lock.json, tests/handlebars-security.test.mjs e este plano.

Regras rastreáveis:

- RN-SCA108-HB001: lookup não expõe Function.constructor, mesmo com acesso permissivo a métodos de protótipo.
- RN-SCA108-HB002: compile/precompile rejeitam AST malformada antes de executar JavaScript injetado; verificar blockParams e profundidade de literal como classes distintas.
- RN-SCA108-HB003: templates legítimos, helpers, partials, loops e geração de arquivos continuam funcionando.

Os testes usam somente marcadores inertes, sem executar comandos do sistema ou acessar serviços. Release 4.7.10 altera a iteração de Map/Set/generators para lazy: [release oficial](https://github.com/handlebars-lang/handlebars.js/releases/tag/v4.7.10). A validação deve incluir o consumidor real de geração, além de strings e AST válidas.

| Origem | Requisito/história/critério | Tarefa | Teste e implementação |
| --- | --- | --- | --- |
| RN-SCA108-HB001 / GHSA-p8wg-vrv2-v86f | FR-HB001, US-HB1: mantenedor bloqueia constructor; SC-HB001: saída vazia | T003–T005 | lookup no teste; override handlebars no package.json e resolução no lockfile |
| RN-SCA108-HB002 / GHSA-8r5x-fm3f-whwj | FR-HB002, US-HB1: rejeitar AST inválida; SC-HB002: erro anterior ao marcador | T003–T005 | compile/precompile blockParams e literal depth; mesmo override |
| RN-SCA108-HB003 / compatibilidade dos geradores | FR-HB003, US-HB1: manter geração; SC-HB003: conteúdo renderizado e arquivo criado | T005 | controle de template/AST e geração Strapi no teste; smoke node-plop adicional |

## Tarefas e controles

- [x] T001 analisar dependências, impacto, testes existentes e avisos oficiais; investigador independente somente leitura.
- [x] T002 H1: usuário aprova alteração de código existente.
- [x] T003 escrever testes primeiro e obter RED: Node test runner com 4.7.9 isolado, quatro falhas de segurança e um controle legítimo passando.
- [x] T004 aplicar override e lockfile; `npm ls handlebars --all` comprova resolução sem 4.7.9.
- [x] T005 GREEN e compatibilidade: `npm run test:security`, geração node-plop isolada e `npm run build` em Node 22.
- [x] T006 revisão independente somente leitura do diff e evidências.
- [ ] T007 registrar Constitution Check antes do PR, commit referenciando #108, publicar PR para develop e obter todos os checks verdes.
- [ ] T008 H2: revisão humana da fatia completa antes de merge; aplicar o mesmo commit à branch de #126 para validar também esse PR, sem integrar develop/main antes de aprovação.

Automatizados existentes: testes CMS, build, SCA HIGH/CRITICAL, scanner de histórico develop/PR e Required Gate agregando backend/frontend/CMS. Novas regressões entram no script test:security já executado em CI. Dependem de revisão humana: H1 já aprovado, H2/merge ainda pendentes; nenhuma alteração de proteção ou supressão é autorizada. Outros domínios continuam cobertos pelos checks existentes, sem mudança de runtime.

## Evidência de validação e Constitution Check

Validação local em cópia temporária Linux, Node 22.23.2:

- `npm ci --no-audit --no-fund`: PASS; instalação de 1301 pacotes com o lockfile da entrega.
- `npm ls handlebars --all`: PASS; @strapi/generators e node-plop usam 4.7.10, deduplicado; nenhuma resolução 4.7.9.
- `npm run test:security`: PASS, 13 testes, 0 falhas, 0 skips, incluindo os 7 existentes e 6 regressões/controles novos.
- Smoke adicional: node-plop gerou arquivo com helper/partial/loop; @strapi/generators gerou controller usando seu template distribuído; ambos PASS em diretório temporário.
- `npm run build`: PASS; TypeScript e painel administrativo compilados, exit 0.
- `git diff --check`: PASS. O lockfile muda somente os metadados Handlebars 4.7.10 e seu range minimist; nenhuma outra versão atualizada.
- Trivy 0.74.0 com digest e argumentos do CI (`fs --scanners vuln --offline-scan --severity HIGH,CRITICAL --exit-code 1 --include-dev-deps`): PASS, exit 0; CMS sem HIGH/CRITICAL não aceitos. Uma supressão preexistente braces/CVE-2026-93687 continua visível, com aceite temporário #108; nenhuma supressão Handlebars adicionada.

Avisos de pacotes transitivos deprecated e MODULE_TYPELESS_PACKAGE_JSON não impediram a instalação/testes; não justificam ampliar este patch. A primeira tentativa de suíte durante instalação incompleta falhou por MODULE_NOT_FOUND; foi substituída pela execução completa acima. CA do Kaspersky usada somente no download temporário local, sem alteração de Dockerfile/Compose/código e sem desligar TLS.

CI remoto será vinculado ao SHA publicado após a execução; resultados locais não equivalem à aprovação do GitHub.

Constitution Check antes do PR, após validações locais:

| Princípio | Evidência e limite | Resultado |
| --- | --- | --- |
| CON-01 | Manifest/lock/testes somente CMS; nenhuma fronteira de produto alterada | PASS |
| CON-02 | Correção transitiva por override existente; nenhuma dependência/abstração nova | PASS |
| CON-03 | Sem identidade/tenant/endpoint novo; testes cross-tenant adicionais não se aplicam | N/A justificado |
| CON-04 | Sem alteração de regras de escala ou decisões operacionais | N/A justificado |
| CON-05 | Fixtures inertes, sem credenciais/PII/schema; CA restrita à execução temporária local | PASS |
| CON-06 | Contratos REST/BFF/OpenAPI e comunicações intactos | N/A justificado |
| CON-07 | Nenhuma UI/claim alterado; build do admin preservado | N/A justificado |
| CON-08 | Matriz RN/FR/US/SC/T; RED, 13 testes GREEN, geração real, build e SCA local PASS | PASS local; CI remoto PENDING |
| CON-09 | develop atual reconfirmada antes da publicação, branch dedicada, H1 explícito e trabalho alheio preservado | PASS preparação; CI/H2/merge PENDING |

Responsável pelos checks pendentes: agente/GitHub; H2 e merge: Wemerson. Os itens pendentes bloqueiam a integração, não são apresentados como concluídos. Não houve emenda, bypass, alteração de proteção ou exceção nova. A ocorrência anterior de governança da limpeza de histórico continua registrada no PR #126 e não é apagada por este check.

Revisão independente concluída em 2026-10-09: nenhum defeito acionável confirmado. Revisor reproduziu cinco testes focados com Node 22.23.2; geração completa/build/SCA foram conferidos por evidência persistida, não reproduzidos independentemente após o container temporário deixar de existir. A instalação Windows incompleta não foi usada para declarar compatibilidade. CI remoto permanece autoridade para o head publicado.

## Riscos e rollback

Possível incompatibilidade de templates de geração: controlar com regressões e smoke do consumidor real. Rollback técnico é revert do commit, mas reintroduz as CVEs e não autoriza merge/release com gate vermelho. TLS deve continuar habilitado; confiança na CA corporativa é configuração local e não entra no repositório. A correção não conclui rotação de credenciais, US2/#106, #99, nem promove main. O ledger parcial do PR #126 permanece válido.
