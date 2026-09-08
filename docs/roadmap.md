# Roadmap — Gestao Inteligente de Escalas

Data de referencia: 2026-09-07.

## Estrategia de entrega

O roadmap prioriza uma entrada de Oceano Azul: PMEs que ainda operam com Excel, WhatsApp, lousa ou papel. A primeira versao vendavel deve resolver escala mensal com templates, feriados, contadores, alertas e publicacao auditavel. Recursos como ponto completo, banco de horas avancado, dimensionamento e IA entram por fases, para evitar competir cedo demais com suites maduras de ponto/RH.

## Backlog por epicos

As fases abaixo continuam representando a evolucao do produto. Para governanca
tecnica no GitHub/Jira, o backlog transversal deve ser classificado nos oito
epicos seguintes, evitando misturar vulnerabilidades, operacao e refatoracoes
sob uma unica categoria de divida tecnica.

| Codigo | Epico | Escopo |
|---|---|---|
| `EPIC-SEC` | Security & IAM | Autenticacao, autorizacao, JWT, sessoes, RBAC/ReBAC, secrets e hardening |
| `EPIC-TENANT` | Multi-Tenant Isolation | Resolucao de tenant, filtros, queries, constraints e testes contra cross-tenant leak |
| `EPIC-PLATFORM` | CI/CD & Engineering Governance | Gates obrigatorios, pipelines, qualidade, supply chain e regras de branch |
| `EPIC-INFRA` | AWS & Production Infrastructure | Rede, compute, banco, storage, secrets, backup, disaster recovery e ambientes |
| `EPIC-BILLING` | Billing, Trial & Plan Enforcement | Trial, assinatura, cobranca, limites de plano, webhooks e reconciliacao |
| `EPIC-OBS` | Observability & Operations | Logs, metricas, traces, alertas, SLOs, runbooks e resposta a incidentes |
| `EPIC-COMP` | LGPD, Audit & Compliance | Consentimento, retencao, direitos do titular, auditoria append-only e evidencias |
| `EPIC-ARCH` | Modular Architecture & Technical Debt | Limites de dominio, arquitetura hexagonal, dependencias e divida estrutural |

### Matriz de prioridade P0-P3

| Prioridade | Criterio | Tratamento esperado |
|---|---|---|
| `P0` | Pode causar vazamento, fraude, indisponibilidade grave ou impedir operacao segura | Interrompe evolucao conflitante, exige responsavel, teste de regressao e gate obrigatorio |
| `P1` | Necessario para operar clientes reais com confiabilidade e governanca | Planejado antes da ampliacao comercial/operacional relacionada |
| `P2` | Reduz fortemente divida, acoplamento e probabilidade de problemas futuros | Priorizado por impacto arquitetural e custo crescente de postergacao |
| `P3` | Otimizacao ou evolucao condicionada a escala ou necessidade comprovada | Executado mediante metrica, capacidade ou demanda validada |

### Gates multi-tenant obrigatorios

- `P0-01`: a identidade do tenant deve vir exclusivamente do principal
  autenticado e ser disponibilizada por um contexto server-side fail-closed.
- `P0-02`: todo recurso tenant-bound deve possuir isolamento na persistencia e
  queries tenant-aware, com constraints e testes de tentativa cross-tenant.
- `P0-03`: migrations, validacao Hibernate e testes de integracao com
  PostgreSQL/Redis reais devem executar em CI obrigatorio, sem falso verde
  quando Docker/Testcontainers estiver indisponivel.
- Nenhuma feature nova pode introduzir recurso tenant-bound enquanto
  `P0-01`, `P0-02` e `P0-03` nao estiverem estabelecidos para o dominio
  afetado. Excecoes exigem decisao arquitetural documentada e aprovacao
  explicita de seguranca.

Estado de referencia em 2026-08-25: a fundacao destes tres gates foi entregue
pelas issues `#27` e `#28`; novos dominios ainda devem demonstrar conformidade
com os mesmos gates antes do merge.

## Mapa por fases

| Fase | Horizonte | Foco | Entregaveis principais |
|---|---:|---|---|
| 0 | Imediato | Auditoria e alinhamento | Build/lint/testes, gaps por modulo, riscos de seguranca, matriz de reaproveitamento |
| 1 | Mes 1 | Fundacao comercial e trial | Leads com UTM/referrer, consentimentos, segmento, faixa de colaboradores, Google SSO/trial |
| 2 | Mes 2 | Escala mensal PME | Calendario mensal, feriados, templates 5x2/6x1/12x36, contadores, grid mensal |
| 3 | Mes 3 | Conformidade e publicacao | Policies backend, alertas, rascunho -> validacao -> publicacao, versionamento e audit log |
| 4 | Mes 4 | Trocas, ausencias e notificacoes | Troca com aceite do colega e gestor, ausencias, ferias, atestados, notificacoes |
| 5 | Mes 5 | Banco de horas e ponto web | Check-in/out/intervalo, saldos, relatorios, preparacao LGPD para geolocalizacao |
| 6 | Mes 6 | Dimensionamento e IA MVP | Demanda x cobertura, alertas de sub/superdimensionamento, IA explicavel para conflito/substituto |

## Progresso atual observado

- Fase 1: em producao local, com captura de leads, campanhas e base de trial/billing
- Fase 2: avancada, com Escala Inteligente ja entregue em SSR, BFF e editor operacional mensal
- Fase 3: iniciada, com ciclo mensal, alertas, ciencia e acoes de publicacao/retificacao/arquivamento ja expostas
- Fase 4 em diante: ainda parcial ou planejada

## Fase 0 — Auditoria, estabilizacao e documentacao

- Validar estado Git antes de qualquer mudanca funcional.
- Executar ou documentar pendencias de `mvn test`, `pnpm run lint`, `pnpm run typecheck`, `pnpm run build`, `strapi build` e `docker compose config`.
- Atualizar mapa de rotas frontend/BFF/backend/OpenAPI.
- Confirmar que `OpenApiController` manual cobre endpoints reais apos qualquer mudanca REST.
- Revisar dados sensiveis em sessao, logs e DTOs.
- Separar claramente backlog de produto pronto, parcial e planejado.

## Fase 1 — Fundacao comercial, lead e trial

- Evoluir formulario comercial para capturar nome, email corporativo, telefone, empresa, faixa de colaboradores, segmento, UTM/referrer, consentimentos e landing/campanha.
- Usar validacao compartilhavel no frontend com Zod e validacao obrigatoria no backend.
- Marcar email pessoal quando usado em campo corporativo.
- Normalizar telefone e preparar formato E.164.
- Criar ou evoluir entidades como `MarketingLead`, `CampaignAttribution`, consentimento LGPD e recomendacao inicial de plano/template.
- Integrar Strapi como fonte editorial de landing pages, formularios e campanhas, mantendo a persistencia operacional no Spring Boot.
- Preparar trial self-service de 14 dias e trial qualificado de 30 dias.

## Fase 2 — Escala mensal para empresas pequenas

- Gerar calendario mensal por mes, ano, timezone e unidade.
- Criar CRUD de feriados nacionais, estaduais, municipais e customizados por tenant/unidade.
- Suportar templates 5x2, 6x1, 12x36 e base extensivel para 4x2, 6x2 e personalizado.
- Calcular contadores mensais por colaborador: dias trabalhados, ausentes, descansos, ferias, faltas, feriados trabalhados, fins de semana trabalhados e horas previstas.
- Implementar legendas configuraveis com impacto em dias, horas, banco de horas, folha e alertas.
- Entregar UI de grid mensal em desktop e mobile.

Status atual:

- entregue calendario mensal, feriados, legendas, ciclo, atribuicoes, contadores, alertas e grid mensal
- entregue produtividade no editor: preencher semana, copiar mes, presets e dif antes do save
- pendente listagem de ciclos por periodo e regras mais profundas de cobertura/capacidade no produto

## Fase 3 — Conformidade, publicacao e auditoria

- Consolidar policies trabalhistas no backend.
- Classificar alertas em criticos e nao criticos.
- Implementar fluxo rascunho -> validacao -> publicacao -> retificacao -> arquivamento.
- Exigir ciencia dos alertas criticos antes de publicar.
- Versionar publicacoes de escala.
- Registrar audit log com ator, entidade, antes/depois, motivo, timestamp, tenant e correlacao de request.
- Criar relatorios mensais basicos para gestor/RH.

## Fase 4 — Trocas, ausencias e notificacoes

- Evoluir maquina de estados de troca: `SOLICITADO`, `EM_ANALISE`, `APROVADO_PELO_COLEGA`, `APROVADO_PELO_GESTOR`, `EFETIVADO`, `REJEITADO` e `CANCELADO`.
- Validar compatibilidade de cargo, setor, unidade, habilidade, descanso e carga horaria.
- Adicionar ferias, atestados, faltas e bloqueios de disponibilidade.
- Criar notificacoes in-app e email para solicitante, colega e gestor.
- Entregar mural simples da escala publicada para colaborador.

## Fase 5 — Banco de horas e ponto web

- Evoluir `TimeRecord` para suportar entrada, saida, inicio/fim de intervalo, origem, timezone e comprovante.
- Criar relatorio de presenca por colaborador, dia e periodo.
- Controlar saldo de banco de horas positivo, negativo, compensado e expirado.
- Parametrizar acordo individual, acordo coletivo, validade e classificacao entre banco de horas e hora extra.
- Preparar geolocalizacao com consentimento explicito, minimizacao, finalidade e retencao.
- Evitar promessa de REP-P/Portaria 671 ate validacao juridica e tecnica especifica.

## Fase 6 — Dimensionamento e IA MVP

- Modelar demanda por setor, unidade, dia e turno.
- Comparar necessario x escalado x disponivel.
- Gerar alertas de subdimensionamento e superdimensionamento.
- Criar IA explicavel para conflito, substituicao e risco de escala.
- Controlar uso de IA por plano, credito e tenant.
- Garantir que IA sugira e explique, mas que validacao e efetivacao continuem no backend.

## Trilhas transversais

- **Arquitetura:** migrar gradualmente para monolito modular com limites por dominio, sem quebrar endpoints existentes.
- **Seguranca:** reforcar multi-tenant, RBAC/ReBAC, minimizacao de dados e protecao de uploads.
- **DevOps:** health checks, Compose por ambiente, secrets obrigatorios fora de dev, backups e CI/CD.
- **Documentacao:** manter `docs/` como fonte conceitual e atualizar OpenAPI manual ao mudar REST.
- **Qualidade:** ampliar testes unitarios de dominio e testes de integracao de autenticacao, JPA, JWT e endpoints.
- **Product/Design/Go-to-Market assistido por IA:** usar o fluxo definido em `docs/ferramentas-ai-product-design-go-to-market.md`, mantendo plugins fora do runtime e submetendo outputs a revisao humana, seguranca, LGPD e governanca do repositorio.
- **AppSec e criterios de aceite:** aplicar `docs/appsec-criterios-de-aceite.md` em contratos, comunicacoes e regras de negocio, com DoR/DoD e gates de seguranca proporcionais ao risco.

## Trilha transversal — AppSec e criterios de aceite

Referencia operacional: `docs/appsec-criterios-de-aceite.md`.

### A0 — Imediato: baseline de seguranca

- Adotar Codex Security como ferramenta assistiva prioritaria para scans, analise e investigacao de seguranca.
- Manter CI/GitHub como autoridade de merge e rastreabilidade.
- Evoluir scanners de dependencias, secrets e SAST/CodeQL quando disponiveis na plataforma.
- Classificar findings por impacto, explorabilidade, superficie e risco multi-tenant.
- Findings criticos como cross-tenant leak, auth bypass, segredo valido exposto ou execucao remota exploravel bloqueiam merge/release.

### A1 — Definition of Ready para novas features

Antes de implementar feature relevante, registrar conforme aplicavel:

- problema, ator e valor;
- criterios de aceite funcionais;
- contrato tecnico ou declaracao de que nao existe mudanca de contrato;
- regras de negocio e invariantes;
- impacto de seguranca, autorizacao e multi-tenant;
- impacto LGPD/compliance;
- estados UX relevantes;
- dependencias externas e riscos.

### A2 — Contratos explicitos

- REST/BFF deve declarar request, response, codigos de erro, autenticacao, autorizacao, tenant e compatibilidade.
- OpenAPI deve acompanhar mudancas REST.
- Eventos/mensageria devem possuir nome/versao, produtor/consumidor, retry, idempotencia e minimizacao de dados quando aplicavel.
- Mudancas breaking exigem estrategia explicita de migracao/versionamento.

### A3 — Comunicacoes confiaveis

- Email, in-app, push e mensagens operacionais devem declarar gatilho, destinatario, canal, conteudo minimo, locale, CTA, retry e comportamento em indisponibilidade.
- Links devem respeitar autorizacao no destino.
- Falha de canal secundario nao deve corromper operacao principal salvo regra de negocio explicita.
- Claims legais/comerciais exigem evidencia e revisao adequada.

### A4 — Regras de negocio testaveis

Cada regra relevante deve explicitar pre-condicoes, atores autorizados, tenant, happy path, bordas, invariantes, erros de dominio, efeitos colaterais, idempotencia/concorrencia, observabilidade e testes.

### A5 — Definition of Done

Uma issue so e concluida quando, conforme aplicavel:

- criterios de aceite passam;
- testes unitarios e integracao/contrato passam;
- autorizacao negativa e tentativa cross-tenant foram cobertas para recurso tenant-bound;
- lint/typecheck/build passam;
- OpenAPI/docs foram atualizados;
- findings de seguranca relevantes foram tratados;
- auditoria/telemetria necessarias existem;
- nenhum segredo ou dado sensivel indevido aparece no diff;
- rollback ou estrategia de reversao e conhecida para mudancas de risco.

## Trilha transversal — Product, Design e Go-to-Market assistidos por plugins

Esta trilha nao substitui as fases de produto. Ela acompanha todas as fases e define em que momento as ferramentas adotadas agregam valor.

| Etapa | Ferramentas prioritarias | Resultado esperado |
|---|---|---|
| Estrategia | Business Strategy Builder, Deep Research | escolhas, trade-offs, nichos, SWOT/Oceano Azul e hipoteses de mercado |
| Posicionamento | B2B Messaging Workshop | ICP, alternativas, diferenciacao, proposta de valor, claims e evidencias |
| Discovery | Product Design, Mobbin, BuildBetter.ai quando houver base de evidencias | jornadas, problemas, referencias, hipoteses e criterios de aceitacao |
| UI/Design System | Figma, Product Design, Themely, Font Pairing | tokens, componentes, variantes, responsividade e acessibilidade |
| Implementacao | Codex/GitHub, Figma e Build Web Data Visualization quando aplicavel | codigo alinhado ao design e aos contratos, com testes e gates |
| Marketing | B2B Messaging Workshop, Creative Production, Strapi/Next.js | homepage, landing pages, campanhas e assets coerentes com claims validados |
| Comercial | Sales | playbook, discovery comercial, demos, business cases, pipeline e follow-up |
| Analytics | Data Analytics, Build Web Data Visualization | metricas definidas, dashboards, funil trial -> paid e aprendizado de produto |
| Evidencias | BuildBetter.ai | consolidacao de feedback/calls/documentos para priorizacao baseada em evidencias |

### Roadmap operacional da trilha

#### T0 — Imediato: governanca

- Adotar `docs/ferramentas-ai-product-design-go-to-market.md` como referencia operacional.
- Nao adicionar SDKs dos plugins ao runtime sem caso de uso de produto, ADR e avaliacao de seguranca/LGPD.
- Usar dados ficticios ou anonimizados em design, marketing e demonstracoes sempre que possivel.
- Manter requisitos, regras de negocio e decisoes finais no repositorio.

#### T1 — Estrategia e positioning

- Revisar Strategy Canvas/Oceano Azul e SWOT com Business Strategy Builder + Deep Research.
- Executar B2B Messaging Workshop para consolidar ICP, alternativas, diferenciacao e proposta de valor.
- Criar matriz de claims: comprovado, hipotese ou proibido ate evidencia.
- Refletir decisoes aprovadas em `docs/Analise-Produto-Arquitetura-Concorrencia-Oceano-Azul.md`, `docs/okr.md` e conteudo editorial quando necessario.

#### T2 — UX/UI antes de features de alto custo

- Usar Mobbin para benchmarking de padroes, sem copiar identidade ou assets proprietarios.
- Usar Product Design para auditar fluxos e prototipar jornadas criticas.
- Consolidar telas aprovadas no Figma e evoluir Design System/tokens antes de espalhar estilos ad hoc no Next.js.
- Tratar acessibilidade, loading, empty, error, disabled e responsividade como criterios de aceite.

#### T3 — Implementacao assistida

Para mudancas relevantes, o Codex deve seguir a ordem obrigatoria:

1. problema;
2. impacto arquitetural;
3. alternativas;
4. recomendacao;
5. riscos;
6. testes;
7. seguranca;
8. impacto multi-tenant;
9. implementacao.

Figma/Product Design servem de entrada visual; regras de negocio e autorizacao continuam no Spring Boot. Build Web Data Visualization deve apoiar dashboards e visualizacoes quantitativas quando houver metrica definida.

#### T4 — Go-to-market

- Usar B2B Messaging Workshop como fonte para narrativa de homepage, landing pages e sales deck.
- Usar Creative Production para campanhas e assets dentro da identidade aprovada.
- Usar Sales para estruturar discovery, demo, business case, proposta e follow-up.
- Manter Strapi como fonte editorial; Spring Boot permanece dono de leads operacionais, trial, billing e limites.

#### T5 — Analytics e aprendizado continuo

- Instrumentar eventos de produto somente apos definir finalidade, minimizacao, tenant e retencao.
- Usar Data Analytics para acquisition, activation, engagement, retention, revenue e conversion.
- Usar Build Web Data Visualization para dashboards do produto e da operacao.
- Introduzir BuildBetter.ai quando houver volume suficiente de feedback, calls e documentos para apoiar priorizacao baseada em evidencias.

Ferramentas explicitamente fora do conjunto adotado neste momento: **UX Pilot, Zoho CRM e Mailchimp**. A adocao futura depende de necessidade comprovada, custo, privacidade, integracao e sobreposicao com a stack atual.

## Nichos prioritarios

### Entrada PLG

- Igrejas e organizacoes com voluntarios/colaboradores fixos.
- Restaurantes e pequenos varejos.
- Pequenas clinicas.
- Empresas de tecnologia com escala hibrida/suporte.
- Facilities/limpeza com poucos postos.

### Venda consultiva

- Seguranca patrimonial.
- Clinicas e hospitais medios.
- Transportadoras e centros de distribuicao.
- Call centers.
- Operacoes 24/7.
