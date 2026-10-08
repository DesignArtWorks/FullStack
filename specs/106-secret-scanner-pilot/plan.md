# Plano — #106, piloto por fases

Data: 2026-10-08 | Spec: [spec.md](spec.md) | Dossiê: [interview.md](interview.md) | Controles: [controls.md](controls.md)
Branch: security/issue-106-secret-scanner-pilot | Base: origin/develop@0efcc18f0dc3a8cc53974de363d3f6128ea73cac
Constituição: [1.0.0](../../.specify/memory/constitution.md) | Execução: [tasks.md](tasks.md)
Estado: H1 US1 APPROVED, 2026-10-08; MVP implementado em scripts/CI, H2 PENDING. Runtime inalterado.

## Proposta e gate socrático

US1 conecta os controles históricos existentes ao CI, testa detecção/exceções/cobertura e corrige documentação com resultados reais. US2 acrescenta diagnóstico de acesso local após definição de comportamento para credencial não mapeável e serviço indisponível. Remediação de histórico real é uma operação específica posterior a inventário e revisão de impacto; não será escondida em um PR comum. Preparação para AWS permanece documental, conforme ADR-005.

Canônicas e decisões confirmadas no diálogo; Q027 autorizou fechamento/preparação. Dossiê CONCLUÍDA com AB-001/002/003/004. Gate de preparação PASS; gate US1 autorizado por H1; integração PENDING por findings/CI/H2; US2 e limpeza dependem adicionalmente das abertas. Não tratar decisão de localidade como autorização para mudar branch protection.

## Technical Context

| Campo | Estado / desenho proposto |
| --- | --- |
| Plataforma | Windows/PowerShell local; GitHub Actions Ubuntu/Docker vigente |
| Scanner | Artefatos existentes de Gitleaks 8.24.2; verificar imagem/digest e compatibilidade em research antes de fixar execução. PowerShell atual fica complementar |
| Dependências | Nenhuma dependência no Spring/Next/Strapi; ferramenta CI fixada por versão/digest e justificativa; não criar SDK AWS |
| Testes | scripts/security/test-secret-scanner.sh já contém fixtures/histórico/fingerprints; testar harness, execução e cobertura real de refs |
| Dados | Git refs/blobs, registros redigidos; credenciais reais somente local e sem logs/env em artefatos; banco aplicação não muda |
| Escala/performance | Histórico de develop/branch PR, sem SLA numérico inventado; observar timeout vigente e custo do fetch/scan |
| Runtime | Spring/Next/PostgreSQL/Strapi/Redis inalterados em US1; validação local US2 sem alterar autenticação dos serviços |

## Project Structure e arquivos

| Arquivo | Ação proposta | Motivo / risco / prova |
| --- | --- | --- |
| specs/106-secret-scanner-pilot/{interview,controls,spec,plan,tasks}.md | Criados nesta preparação | Fonte, controles, contratos e tarefas; somente Markdown |
| .github/workflows/backend-integration.yml | Alterar após H1 | Materializar develop/head PR com história completa e incorporar scanner/testes; preservar os oito checks, permissões contents:read, isolamento de forks |
| scripts/security/test-secret-scanner.sh | Reusar/ampliar após H1 | Cobertura de referências, negativo histórico, exceção exata e resultado incorreto/erro; fixtures sintéticas não autenticam externamente |
| scripts/security/scan-secret-history.sh | Criar após H1, se necessário | Concentrar resolução de refs/scan redigido; não expor valores nem aceitar erro como sucesso |
| .gitleaks.toml / .gitleaksignore | Inspecionar; alterar só por ocorrência revisada | Não excluir diretórios/commits inteiros; real não vira falso positivo. Allowlist atual exige inventário |
| docs/secret-scanning-issue-106.md | Corrigir após H1 | Refletir recorte develop/headPR e provas; não alegar cobertura --all de refs ausentes |
| docs/ci-gates.md | Conciliar após H1 | Inventário real de checks, testes frontend e secret gate, sem ampliar política de bypass |
| scripts/security/validate-local-credentials.* e testes correspondentes | Desenho/linguagem após AB-001/003 e H1 próprio | US2: adaptadores por tipo/serviço; nenhuma chamada externa/credencial em argumentos/logs; arquivo/extensão ainda não escolhido |
| docs/secrets-rotacao-issue-49.md e relatório de limpeza | Proposta depois do inventário | Coordenar referências/clones, rotação e recovery; não executar rewrite por preparar este plano |

Nenhuma edição proposta em controllers, DTOs, JPA, features React ou contratos BFF nesta US1. Layout, rotas e trabalho local de UI preservados. Consultar Backend/cms-strapi/AGENTS.md se uma mudança futura tocar o CMS.

## Arquitetura, contratos, operação

O controle pertence à governança/CI, não ao domínio de escala. Domínio hexagonal permanece parcialmente migrado; GenerateScheduleService ainda depende de DTO/entity. Não instalar Spring Modulith/ArchUnit para resolver problema transversal nesta issue. Validação local é tooling separado com adapters de autenticação, sem transferir autoridade do Spring ou usar IA para escolher tenant.

Produtor/consumidor US1: scanner/test-harness → job CI/mantenedor. Interface de tooling: resultado sanitizado por ref/SHA/ocorrência, status distinguindo detecção e erro; definir códigos de saída concretos no research. Não há REST/evento público novo, OpenAPI N/A. Não retornar valores, hashes reversíveis de segredo, headers/cookies ou URLs com senha em relatórios.

US2 usa contas/alvos locais explícitos e mapping verificável entre tipo, ocorrência e serviço. .envs são fontes de credenciais, não nomes arbitrários de destino confiável. Não adivinhar destinatário, fazer brute force, criar usuário ou efetivar operação operacional para testar. Signing keys não são credenciais de login; definir alternativa comprovável ou estado NÃO_VERIFICÁVEL em AB-003, sem alegar invalidez.

A autenticação local não é requisito para o scanner estático detectar padrões externos. Falha de ferramenta/checkout deve continuar FAIL. AB-001 define efeito de falha de serviço sobre diagnóstico local, sem alterar o finding externo confirmado em Q025. Sem dados reais de tenant nos fixtures; autenticação pode gerar sessão/auditoria local e deve estar no plano de testes.

Remediação histórica: inventariar detecções com redaction e vínculo às refs; separar ocorrências sintéticas/falso positivo/local real/externo suspeito. Plano de limpeza deve informar refs afetadas, backup privado e retenção, clones/PRs/fingerprints e recuperação sem reexpor segredo ou burlar governança. Decisão de retirar segredo do histórico não autoriza force-push concreto antes dessa revisão. Nenhum valor secreto entra no repo ou conversa.

## Constitution Check inicial

| Princípio | Aplicação / evidência de desenho | Estado / dono |
| --- | --- | --- |
| CON-01 | Tooling CI separado de runtime, CMS editorial e BFF/Spring preservados | PASS no desenho |
| CON-02 | Não altera domínio; registra dívida parcial e evita refatoração transversal | PASS no desenho |
| CON-03 | Sem contrato tenant-bound novo; alvos US2 locais/autorizados; não inferir tenant do scanner | PENDING US2 / AB-003, Wemerson |
| CON-04 | Findings/erros distintos, decisão inconclusiva não implícita | PENDING US2 / AB-001, Wemerson |
| CON-05 | Redaction, .env ignorado, sem valor externo; rewrite/rotação não executados | PENDING remediação / AB-002, Wemerson |
| CON-06 | Contrato interno de tooling, sem REST/OpenAPI novo | PASS no desenho; limites US2 pendentes |
| CON-07 | Sem UI/claim legal; scanner limpo não é segurança absoluta | N/A UI; PASS limite de claim |
| CON-08 | RN/FR/US/SC/T; testes negativos isolados e CI preservado | PASS no desenho; implementação/testes PENDING |
| CON-09 | Branch isolada da base atual, H1/H2/H3, sem bypass/promover main | PASS preparação; H1 PENDING Wemerson |

Pendências são por recorte; não declarar conformidade do escopo inteiro nem usar tabela como exceção constitucional.

## Documentos de apoio e pesquisa

- research.md: produzir em T004; verificar primariamente documentação oficial Gitleaks para versão/digest/exit statuses, fetch de refs de PR (fork inclusive), redaction e custos. Não fabricar validação de imagem.
- contracts/local-validation.md: necessário para US2 após AB-001/003; tipo de credencial, alvo autorizado, protocolo, tentativas/timeouts, resultado inconclusivo, sessão/efeitos e logs.
- data-model.md: N/A US1, não há schema/migration; registros redigidos internos descritos no contrato tooling.
- quickstart: atualizar docs/secret-scanning-issue-106.md com comandos realmente usados, versões e exemplo redigido; não criar comando que ainda não existe como prova executada.

## Fases, validação e checkpoints

| Fase | Prova / critério | Parada |
| --- | --- | --- |
| Setup/preparação | Base, dossiê real, controles e spec/plan/tasks revisáveis | H1 antes dos scripts/workflow |
| Foundational | Pesquisa, harness/refs e casos falhos pertinentes do controle; inventário sanitizado de exceções | H3 se novo tipo/critério/risco mudar aceite |
| US1/MVP | Testes negativos detectam, placeholder aprovado passa, erro falha, histórico develop/headPR comprovado; CI completo | H2 após demonstração, sem auto-merge |
| US2 | AB-001/003 resolvidas, contrato local e H1 próprio; testes isolados e acesso real redigido quando autorizado | Nova revisão após recorte; não enviar a provedores |
| Remediação histórica | AB-002 resolvida, plano e ensaio em cópia privada; autorização operacional específica | Não executar por aprovação genérica da US1 |
| Polish/entrega | Docs corretas, CON final, hashes/CI/PR/rollback; integração no destino autorizado | develop; main/#99/release permanecem sujeitos a gate separado |

US1 testes: harness Gitleaks fixado, Git sintético isolado, erro deliberado de execução distinguido de finding, nova ocorrência pós-exceção, segredo só na história de develop, só na branch PR e remoção no HEAD. Não adicionar segredos válidos às fixtures para provar detecção; não reduzir teste a substring que espelha o código.

Preparação documental: links, cinco seções, 27 respostas reais, IDs, duas tabelas CON e diff. Runtime/lint/build/Docker local N/A nesta preparação. Após alterar workflow/scripts, executar harness/scan e CI obrigatório com todos os jobs; não inventar que um scanner histórico verde prova revogação ou zero segredos. Se runtime backend mudar excepcionalmente, aplicar AGENTS completo com Docker health/Swagger/OpenAPI, não ignorar gate.

## Matriz de rastreabilidade

| Regra / requisito | Tarefas | Teste/verificação | Artefato |
| --- | --- | --- | --- |
| RN-SEC106-001; FR-001/003; SC-001/003 | T004–T007 | Harness histórico/refs/erro e CI | workflow / scripts |
| RN-SEC106-002; FR-002/004; SC-002/004 | T004–T007 | Exceção exata, sibling/reintrodução, sem rede externa | .gitleaks* / testes |
| RN-SEC106-003; FR-006; SC-006 | T009–T010 | Contrato e sandbox local após abertas | tooling local / relatório redigido |
| RN-SEC106-004; FR-005/007; SC-005/007 | T008/T011 | Inventário/ensaio/rotação verificáveis | plano limpeza / evidências #99 |
| FR-008/009/010; SC-008/009/010 | T003/T007/T012 | Links/docs coerentes, availability real, ledger | controls / docs / PR |

## Constitution Check final — estado da preparação antes de implementar

Head base: 0efcc18; arquivos novos ainda sem commit/PR. Nenhum teste runtime deste piloto executado.

| Princípio | Evidência desta fase | Resultado / pendência |
| --- | --- | --- |
| CON-01 | Apenas cinco Markdown novos; sem runtime | PASS preparação |
| CON-02 | Dívida arquitetural explicitada, sem código novo | PASS preparação |
| CON-03 | Nenhuma autenticação/tenant alterado ou tentado | N/A execução nesta fase; US2 PENDING AB-003 |
| CON-04 | Abertas por recorte registradas; nenhum resultado de negócio alterado | PASS preparação; AB-001 PENDING |
| CON-05 | Sem coleta/artefato de segredo real; histórico/.env não editados | PASS preparação; AB-002 PENDING |
| CON-06 | Nenhum REST/evento/contrato público alterado | N/A runtime; proposta local PENDING |
| CON-07 | Sem UI/claim; limitações do scanner documentadas | N/A UI; PASS documentação |
| CON-08 | Matriz RN/FR/SC/T revisada; links/cinco seções/dois checks CON/diff/scanner local aprovados, registro no tasks | PASS preparação; código/CI PENDING |
| CON-09 | Base/branch isoladas, sem commit/merge/push inferidos | PASS preparação; H1/CI/entrega PENDING |

Repetir o check com diff/resultados/head efetivos antes do PR; esta tabela não substitui a revisão final da implementação.

## Rollback e limites

Preparação: remover/reverter somente estes documentos na branch isolada. US1: revert do PR preservando controles existentes; regressão no workflow deve falhar e ser corrigida via PR, sem remover required gate. US2: tooling local desativado, sem migração de banco; invalidar sessões de teste conforme contrato. Reescrita: recovery próprio a definir antes de executar, sem restaurar exposição pública por conveniência.

Estado final de entrega constará do PR/issue com SHA/CI/aprovação reais; tasks é snapshot versionado. Não concluir #99 nem enviar main enquanto faltarem os critérios daquela agregadora.

## Constitution Check efetivo — MVP US1

CON-01/02/03/06/07: runtime, tenant, contratos e UI inalterados. CON-04: códigos 0/42/2 distinguem ausência de finding, finding e erro. CON-05: containers read-only sem rede/redaction; duas exceções sintéticas; findings reais bloqueiam; AB-002 pendente. CON-08: RED/GREEN, IDs e evidência no tasks/research; CI remoto pendente. CON-09: H1 aprovado, branch isolada; parar em H2. Sem autorização de merge, US2 ou rewrite. As tabelas anteriores preservam o snapshot de preparação, não execução posterior.

Arquivo adicional de teste: scripts/security/test-secret-history.sh. Pesquisa: research.md. Histórico real BLOCKED; funcionamento esperado do gate perante ocorrências não excepcionadas, impedindo integração.

## Estado após pedido de remediação — 2026-10-08

O pedido humano para tratar ocorrências libera o plano operacional, correção CMS e ensaio isolado. Ver history-remediation-plan.md e o registro posterior em tasks.md. Ensaio: 15 ocorrências removidas, uma fixture sintética com exceção exata proposta, scanner e ambos os harnesses PASS; sete testes CMS e build Docker/Node 22 PASS. Código atual passa a consumir DATABASE_PASSWORD sem fallback embutido.

CON-01/02/03/06/07: nenhuma dependência runtime, regra de domínio/tenant, contrato ou UI alterados. CON-04/05: classificação específica com evidência, sem alargar allowlists; valores privados somente temporários; local .env preservado; não inferir revogação. CON-08: RED/GREEN de credenciais CMS, IDs, scanner completo e revisão independente registrados. CON-09: cópia independente e nenhuma ref/proteção remota alterada; H3 da manutenção e CI remoto PENDING. Não concluir US2, merge ou #99 com este ensaio.

## Constitution Check pós-execução e pré-PR corretivo — 2026-10-08

Escopo: execução efetiva da manutenção #106/US1 e esta correção documental sobre develop 0e9e58dfbdeb46c3d99107739566d9dd10dd62da, depois dos merges humanos #124/#125. Evidências: runs [37833171774](https://github.com/DesignArtWorks/FullStack/actions/runs/37833171774) e [37834019034](https://github.com/DesignArtWorks/FullStack/actions/runs/37834019034), oito jobs SUCCESS em cada head; plano operacional, diff de CMS/scripts/configuração e registro de restauração.

**Lacuna de governança registrada:** não havia tabela CON-01–CON-09 consolidada e versionada da publicação efetiva antes dos PRs #124/#125. As conferências anteriores eram do MVP/ensaio e ficaram como snapshots pendentes. Esta revisão é posterior; não comprova retrospectivamente o cumprimento do gate pré-PR. A restrição de force push ao ator também não persistiu na interface durante a janela e todas as proteções foram restauradas ao detectar. A instrução humana excepcional autorizou somente este caso; não houve emenda da constituição nem autorização permanente. Não declarar conformidade integral, fechar #106/#99 ou promover main por esta regularização. Wemerson responde pela decisão sobre a lacuna registrada; os fatos e os resultados técnicos continuam verificáveis.

| Princípio | Aplicabilidade e evidência efetiva | Resultado e pendência / responsável |
| --- | --- | --- |
| CON-01 | CMS continua editorial; alteração apenas em configuração de credencial. Sem acesso frontend a banco ou alteração de fontes operacionais. | PASS no recorte; nenhuma pendência nova |
| CON-02 | Sem novo framework/dependência de runtime; domínio/controllers/frontend inalterados. Alteração de configuração CMS e tooling, preservando a evolução incremental. | PASS no recorte; migração hexagonal completa não alegada |
| CON-03 | Nenhum endpoint, JWT runtime, tenant ou autorização alterado; fixture exclusiva do perfil test, sem autenticação externa. | N/A novos testes de autorização neste diff; US2/alvos locais PENDING, Wemerson |
| CON-04 | Regras operacionais inalteradas. Scanner distingue 0/42/2; falha de execução e cobertura incompleta bloqueiam, conforme RN-SEC106-001/002. | PASS controle US1; nenhuma decisão de negócio nova |
| CON-05 | Remoção de 15 ocorrências e fingerprint sintético exato; CMS recebe senha por env; redaction/read-only/sem rede; .env locais preservados; CI dos dois históricos PASS. | PASS limpeza das refs selecionadas; rotação e demais refs/clones PENDING, Wemerson; não prova revogação |
| CON-06 | Nenhum REST/BFF/evento/erro público alterado; OpenAPI vigente preservado. Contrato tooling de saída 0/42/2 documentado e testado. | PASS tooling; N/A atualização REST/OpenAPI; contrato de US2 PENDING |
| CON-07 | Sem interface, claim comercial ou efeito operacional de IA. Documentação declara limites do scan, escopo e execução posterior. | N/A testes visuais; PASS transparência do recorte |
| CON-08 | RED/GREEN pertinentes dos harnesses e das credenciais CMS; sete testes CMS e build no CI; matriz RN/FR/T e commits/PR/runs verificáveis; esta correção diferencia PARTIAL de DONE. | PASS evidência técnica do recorte; registro constitucional tardio explicitado, sem falsificar momento de aprovação |
| CON-09 | Branchs isoladas e trabalho local preservado; manutenção atômica excepcional aprovada; Required Gate manteve-se; proteções restauradas; #124/#125 mesclados por Wemerson após checks. | PARTIAL governança: gate constitucional pré-PR não documentado contemporaneamente e incidente de restrição do ator; Wemerson. main/fechamento #106/#99 não liberados |

Este check antecede o PR corretivo que adiciona esta seção; o SHA desse documento e os checks do novo PR ficam no corpo do PR para evitar autorreferência. Não fabricar uma execução histórica do check. A correção documental pode avançar para revisão com as lacunas explícitas; qualquer entrega que dependa de conformidade integral permanece bloqueada.