# Especificação — scanner histórico e validação local

Spec: 106-secret-scanner-pilot | Issue: #106 | Data: 2026-10-08 | Estado: **proposta para H1**
Dossiê: [interview.md](interview.md), CONCLUÍDA com abertas por recorte; Q027 autoriza preparação, não implementação. Modo por fases.

## Problema, ator e valor

O gate atual inspeciona arquivos rastreados por quatro padrões e não materializa todo o histórico aprovado. Artefatos de scanner histórico e testes existem, mas o workflow não os chama. Wemerson/mantenedores precisam detectar exposições e revisar exceções com evidência, sem interpretar scanner limpo como revogação ou segurança absoluta. A #99 permanece agregadora/release blocker; concluir esta filha não conclui a agregadora.

## Histórias priorizadas

### US1 — scanner e controles demonstráveis (P1, MVP)

Como mantenedor, quero que o PR execute a detecção histórica e controles negativos para distinguir scanner funcional, ocorrência aceita, finding bloqueante e erro de execução.

- Dado um segredo sintético introduzido e removido do HEAD em repositório isolado, o scanner deve detectar o commit antigo sem acesso a serviço externo.
- Dada uma exceção específica aprovada, ela permite somente a ocorrência correspondente; nova cópia, arquivo ou outra ocorrência continua detectável.
- Dado um PR, a cobertura declarada corresponde ao histórico efetivamente materializado de develop e da branch do PR.
- Erro de checkout/scanner não é ausência de finding; placeholders aceitos não fazem todos os testes negativos passarem silenciosamente.

### US2 — diagnóstico de acesso local (P2 no sequenciamento, não prioridade de backlog)

Como responsável local, quero verificar credenciais provenientes do histórico e .env ignorados contra Spring/Next/PostgreSQL/Strapi/Redis autorizados, sem enviar valores para serviços externos nem efetivar operação de negócio.

AB-001/AB-003 bloqueiam implementação desta história. Não usar credencial de produção; não testar AWS/GitHub/Stripe/Slack ou outro provedor. Resultado de acesso, indisponibilidade e credencial não mapeável devem ser diferentes; efeito do inconclusivo sobre gate local ainda aberto.

## Regras de origem

| ID estável | Regra | Fonte / responsável |
| --- | --- | --- |
| RN-SEC106-001 | Scanner cobre história de develop e branch do PR; erros de execução não são PASS | PROV-006/015; #99 / Wemerson |
| RN-SEC106-002 | Exceções revisáveis só para placeholders, fictícios de teste e falso positivo comprovado; controles negativos isolados continuam detectados | PROV-007/008/013; Wemerson |
| RN-SEC106-003 | Autenticação limitada a serviços locais e às duas fontes aprovadas; sem envio externo | PROV-010/011/013; Wemerson; detalhes AB-001/003 |
| RN-SEC106-004 | Valores históricos são expostos; limpeza requer plano específico; rotação local antes do gate final #99 | PROV-003/004/005/017; Wemerson |

### Aceite por regra

| Campo | RN-SEC106-001 | RN-SEC106-002 |
| --- | --- | --- |
| ID/origem/FR/US | PROV-006/015; FR-001/003; US1 | PROV-007/008/013; FR-002/003/004; US1 |
| Pré-condições | Refs aprovadas materializadas; scanner disponível | Ocorrência redigida identificável; justificativa e revisão |
| Atores autorizados | CI e mantenedor local | Wemerson revisa; agente não fabrica aprovação |
| Tenant | Global: controle do repo; sem dados de tenant | Global: controle do repo; sem autorização runtime |
| Happy path | Finding detectado, cobertura declarada verdadeira | Exceção exata permite ocorrência aprovada |
| Bordas/invariantes | Segredo removido do HEAD detectável; outras refs fora do recorte | Reintrodução/sibling continua detectado; credencial real não vira falso positivo |
| Erros | Scanner/checkout indisponível falha explicitamente | Exceção malformada/ampla rejeitada; não aceita por silêncio |
| Efeitos colaterais | Resultado/artefato redigido; sem autenticação | Alteração de allowlist rastreada/revisada |
| Concorrência/idempotência | Varredura somente leitura, resultado por snapshot; reexecução não altera Git | Mesma exceção aplicada ao mesmo fingerprint; não amplia alcance |
| Observabilidade | Ref/SHA, regra, caminho, status; nunca valor secreto | ID da ocorrência/decisão/revisor; sem valor |
| Testes | Fixtures isoladas, história, erro do scanner/cobertura | Placeholder, falso positivo revisado, exceção exata e nova ocorrência |

| Campo | RN-SEC106-003 | RN-SEC106-004 |
| --- | --- | --- |
| ID/origem/FR/US | PROV-010/011/013; FR-006; US2 | PROV-003/004/005/017; FR-005/007; remediação operacional |
| Pré-condições | Alvo local explícito, credencial associada, ambiente autorizado; AB-001/003 resolvidas | Inventário redigido, refs/clones identificados; AB-002 resolvida |
| Atores autorizados | Operador local autorizado por Wemerson; não CI compartilhado com secrets reais | Wemerson coordena; execução depende de plano específico |
| Tenant | Pré-tenant para login; não efetivar operação tenant-bound | Global ao repo; não alterar dados de negócio |
| Happy path | Acesso comprovado sem registrar token/valor e sem mutação operacional | Código sanitizado; histórico no recorte aprovado verificado; rotação evidenciada no marco |
| Bordas/invariantes | Sem acesso externo; signing key não é senha; inconclusivo separado | Outras cópias podem persistir; limpeza não revoga; não bypass de protections |
| Erros | AB-001 define inconclusivo/indisponível; não tratar acesso não testável como inválido | Conflito/perda de refs impede execução; backup/recovery sujeitos ao plano |
| Efeitos colaterais | Sessões/logs de autenticação e rate limit possíveis; não publicar escala nem alterar cadastro | SHAs/refs e sessões/chaves podem mudar; coordenar antes |
| Concorrência/idempotência | Limites de tentativa e sessão por tipo dependem de AB-003 | Operação única coordenada; não repetir rewrite/force-push automaticamente |
| Observabilidade | Resultado por alvo/ID redigido, sem credenciais | SHAs/refs afetadas, operador, prova redigida, plano de recuperação |
| Testes | Negativo, sucesso e indisponível em sandbox local depois de H1 próprio | Ensaio em cópia isolada e nova varredura; nenhuma execução autorizada nesta preparação |

## Requisitos e sucesso

| FR | Comportamento | Origem / história | Critério de sucesso |
| --- | --- | --- | --- |
| FR-001 | Inspecionar todo histórico aprovado, com cobertura de refs/SHA verificável | RN-001; US1 | SC-001: segredo removido do HEAD continua detectado |
| FR-002 | Aceitar apenas exceções específicas justificadas/revisadas | RN-002; US1 | SC-002: exceção não oculta outra ocorrência nem sua reintrodução |
| FR-003 | Provar detecção por controles sintéticos isolados sem credencial externa real | RN-001/002; US1 | SC-003: PEM/GitHub/Slack/AWS detectados, placeholder permitido; erro de execução distinguido |
| FR-004 | Finding externo real/suspeito bloqueia até análise; não autenticar externamente | RN-002/003; US1 | SC-004: nenhum request externo de validação; findings não aceitos não viram PASS |
| FR-005 | Preparar limpeza de código/histórico sem execução implícita | RN-004; operação | SC-005: inventário redigido e plano de impacto/recovery revisados antes de executar |
| FR-006 | Diagnosticar acesso com credenciais históricas/.env nos cinco serviços locais | RN-003; US2 | SC-006: sucesso/negado/inconclusivo são distinguíveis; AB-001/003 resolvidas |
| FR-007 | Registrar aceitação temporária local e marco de rotação | RN-004 | SC-007: owner Wemerson, #103/#106/#109 antes do gate final #99; sem alegação de revogação |
| FR-008 | Preparar referências à AWS conforme ADR-005, sem recursos novos | PROV-014 | SC-008: documentação não contém valor secreto nem provisionamento |
| FR-009 | Corrigir estado dos controles e manter trilha tarefa/commit/PR | PROV-002/016; controles | SC-009: docs correspondem ao workflow/resultados efetivos; ledger distingue preparado/integrado |
| FR-010 | Verificar disponibilidade de push protection e registrar próximo passo | Aceite #99 | SC-010: configuração real consultada ou pendência explícita AB-004; nenhuma ativação inventada |

RN-001/002/003/004 abreviam RN-SEC106-001/002/003/004 somente nesta tabela.

## Limites e exclusões

Sem contrato REST novo, sem mudança em domínio de escala/tenant, sem refatoração transversal hexagonal, sem SDK AWS/runtime novo, sem aplicação de infraestrutura. Campos sensíveis são manipulados localmente em memória e relatórios redigidos; jamais versionar .env, tokens, saída raw do scanner ou fingerprint de dispositivo/dados pessoais desnecessários. Não declarar #99 concluída, promover main ou fazer deploy por finalizar o MVP.

Allowlist histórica atual inclui ocorrências descritas como valores locais reais: inventariar/revisar, sem copiar cegamente como falso positivo. Se não houver resolução, registrar finding e manter integração pendente; não relaxar o scanner para conseguir CI verde. A indisponibilidade de validação local não deve ser generalizada como permissão para ignorar segredos externos.

## Definition of Ready

- [x] Problema, responsável, fontes e modo por fases registrados.
- [x] Dossiê com respostas reais, canônicas, fases/categorias e refinamentos.
- [x] Contrato/runtime sem alteração nesta preparação; dados/sigilo/tenant identificados.
- [x] US1 separada de autenticação local e remediação histórica.
- [ ] H1 do plano/entendimento US1 confirmado por Wemerson.
- [ ] AB-001/003 resolvidas para US2; AB-002 para limpeza; AB-004 para configuração externa.

Spec proposta não libera implementação. Critérios de saída por recorte estão em [tasks.md](tasks.md).

H1 US1 aprovado por Wemerson em 2026-10-08, resposta “sim”. Implementação deste recorte autorizada; H2/US2/limpeza pendentes. Ver ledger para execução.
