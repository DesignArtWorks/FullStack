# Ferramentas de IA, Product Design e Go-to-Market — Escala

Data de referencia: 2026-09-07.

## Objetivo

Este documento define como o projeto Escala deve usar as ferramentas/plugins adotados durante discovery, estrategia, UX/UI, implementacao, marketing, vendas e analytics.

As ferramentas descritas aqui sao **ferramentas de trabalho do time e do Codex/ChatGPT**. Elas nao fazem parte do runtime do SaaS, nao devem ser adicionadas automaticamente como dependencias do Spring Boot, Next.js, Strapi ou PostgreSQL e nao substituem decisoes arquiteturais, revisao humana, testes ou validacao juridica.

## Principio de uso

O fluxo preferencial e:

```text
Estrategia e mercado
        ↓
Discovery e evidencias
        ↓
Posicionamento e proposta de valor
        ↓
UX / fluxos / prototipos
        ↓
Design System
        ↓
Implementacao no repositorio
        ↓
Validacao tecnica e de seguranca
        ↓
Go-to-market e vendas
        ↓
Analytics e aprendizado
        ↺
```

Nenhum output de ferramenta externa e considerado requisito, decisao arquitetural ou verdade de produto sem revisao e registro no repositorio quando aplicavel.

## Ferramentas adotadas

### Product Design

Uso principal:

- explorar fluxos e alternativas de produto;
- auditar UX de telas e jornadas existentes;
- prototipar onboarding, dashboard, escala mensal, trocas, configuracoes e experiencias mobile;
- validar hierarquia, navegacao, estados vazios, erros e acessibilidade antes de implementar.

Gate para implementacao:

- requisitos e regras de negocio devem continuar documentados no repositorio;
- prototipo nao define contrato REST nem regra de dominio;
- fluxos tenant-bound devem explicitar contexto, permissao e estados de erro.

### Figma

Uso principal:

- fonte de verdade visual quando houver design aprovado;
- Design System, tokens, componentes e variantes;
- handoff design -> codigo;
- Code Connect/regras de componentes quando aplicavel.

Diretriz:

- componentes compartilhados devem convergir para tokens reutilizaveis;
- nao copiar estilos isolados para cada pagina;
- mudanca no Design System deve considerar acessibilidade, responsividade e impacto nas telas existentes.

### Mobbin

Uso principal:

- pesquisa de referencias e padroes UX/UI de produtos reais;
- benchmarking de onboarding, dashboards, tabelas, filtros, calendarios, configuracoes, navegacao e mobile.

Restricao:

- usar como referencia, nao como fonte para clonagem de identidade, assets ou experiencia proprietaria de terceiros.

### Themely Design+Style Generator

Uso principal:

- explorar direcoes visuais para dashboard, landing pages e superficies internas;
- comparar combinacoes de layout, espacamento, tipografia e linguagem visual antes de consolidar no Figma.

Uso secundario; Figma continua sendo a fonte de verdade do design aprovado.

### Font Pairing: Design & Brands

Uso principal:

- explorar combinacoes tipograficas para marca, marketing e Design System.

Qualquer fonte adotada deve ser validada quanto a licenca, performance web, legibilidade, caracteres PT-BR e estrategia de carregamento no Next.js.

### B2B Messaging Workshop

Uso principal:

- ICP;
- alternativas atuais do cliente;
- problemas prioritarios;
- diferenciacao;
- proposta de valor;
- positioning;
- claims e evidencias;
- Product Marketing source of truth.

Diretriz:

- claims comerciais devem ser classificados como comprovados, hipotese a validar ou proibidos ate evidencia suficiente;
- evitar promessas absolutas de conformidade trabalhista, LGPD ou Portaria 671 sem validacao tecnica/juridica correspondente.

### Business Strategy Builder

Uso principal:

- consolidar estrategia e trade-offs;
- Business Strategy Canvas;
- escolha de segmentos prioritarios;
- alinhamento entre produto, operacao e go-to-market.

Deve ser usado em conjunto com os documentos de Oceano Azul, SWOT, OKRs e roadmap existentes, sem substitui-los automaticamente.

### Sales

Uso principal:

- estruturar processo comercial B2B;
- discovery comercial;
- preparacao de reunioes e demos;
- business cases;
- pipeline e forecast quando houver dados;
- follow-up e materiais de decisao.

A ferramenta nao deve receber dados pessoais ou comerciais de clientes alem do necessario e permitido.

### Creative Production

Uso principal:

- moodboards;
- conceitos de campanha;
- assets de lancamento;
- criativos e social posts;
- imagens de produto e marketing.

Diretriz:

- identidade visual final deve respeitar o Design System e guidelines da marca;
- conteudo promocional deve respeitar claims validados pelo B2B Messaging Workshop e pelo repositorio.

### Data Analytics

Uso principal:

- analise de produto e negocio;
- acquisition, activation, engagement, retention, revenue e conversion;
- validacao de hipoteses;
- analise de trial -> paid;
- segmentacao e acompanhamento de uso.

Nao deve existir analise cross-tenant baseada em dados identificaveis sem desenho explicito de privacidade e autorizacao. Preferir metricas agregadas e minimizadas.

### Build Web Data Visualization

Uso principal no Codex:

- projetar e implementar dashboards, graficos, Gantt, UML e visualizacoes do produto;
- revisar clareza, acessibilidade e performance de visualizacoes;
- transformar metricas validadas em componentes Next.js/React quando fizer sentido.

Visualizacao nao substitui definicao de metrica. Toda metrica deve ter owner, formula e fonte de dados conhecida.

### BuildBetter.ai

Uso principal, quando houver volume suficiente de evidencias:

- consolidar feedback de clientes, entrevistas, calls, documentos e sinais de produto;
- ligar evidencia de cliente a decisoes de produto e engenharia;
- apoiar priorizacao baseada em evidencias.

Nao deve se tornar fonte unica de verdade. Requisitos aprovados continuam no backlog e docs oficiais.

### Deep Research

Uso principal:

- pesquisa de mercado e concorrencia;
- regulacao e referencias publicas;
- tendencias de workforce management;
- estudo de nichos, pricing e alternativas.

Resultados externos devem ser citados e diferenciados de fatos observados no codigo ou de dados internos.

## Ferramentas nao adotadas neste momento

- UX Pilot;
- Zoho CRM;
- Mailchimp.

A nao adocao atual nao e proibicao definitiva. A entrada futura depende de problema real, beneficio mensuravel, custo, risco, privacidade, integracao e sobreposicao com ferramentas ja adotadas.

## Roadmap de uso das ferramentas

### Trilha T0 — Governanca imediata

1. Manter este documento e `AGENTS.md` como orientacao para o Codex.
2. Nao instalar SDKs ou dependencias de plugins no runtime sem ADR e caso de uso de produto.
3. Definir quais informacoes podem sair do repositorio/ambiente e quais sao restritas.
4. Usar dados ficticios ou anonimizados em exploracoes de design e marketing.
5. Registrar decisoes que afetem produto/arquitetura nos documentos oficiais.

### Trilha T1 — Estrategia e posicionamento

Ferramentas: Business Strategy Builder, B2B Messaging Workshop, Deep Research.

Entregaveis:

- ICP priorizado;
- Strategy Canvas/Oceano Azul revisado;
- SWOT revisada com evidencias;
- proposta de valor por segmento;
- matriz de claims/evidencias;
- hipoteses de pricing e go-to-market;
- atualizacao de OKRs quando necessario.

### Trilha T2 — Product discovery e UX

Ferramentas: Product Design, Mobbin, BuildBetter.ai quando houver evidencia interna suficiente.

Entregaveis:

- jornadas criticas;
- user flows;
- problemas de usabilidade;
- prototipos antes de features de alto custo;
- criterios de aceitacao de UX e acessibilidade;
- backlog priorizado por valor, risco e evidencia.

### Trilha T3 — Design System e UI

Ferramentas: Figma, Product Design, Themely, Font Pairing.

Entregaveis:

- foundations/tokens;
- tipografia e escala visual;
- componentes e variantes;
- estados loading/error/empty/disabled;
- padroes de tabelas, filtros, formularios e calendario;
- responsividade e acessibilidade WCAG como criterio de aceite.

### Trilha T4 — Implementacao assistida pelo Codex

Ferramentas: GitHub, Figma/Product Design quando houver design aprovado, Build Web Data Visualization para interfaces quantitativas.

Ordem obrigatoria antes de implementar mudanca relevante:

1. explicar o problema;
2. identificar impacto arquitetural;
3. apresentar alternativas;
4. recomendar uma solucao;
5. indicar riscos;
6. propor testes;
7. avaliar seguranca;
8. avaliar impacto multi-tenant;
9. somente entao implementar.

Durante a implementacao:

- backend continua sendo autoridade de regras e autorizacao;
- frontend nao deve confiar em tenantId fornecido pelo cliente;
- manter contratos REST explicitos;
- executar gates de teste/lint/build;
- atualizar OpenAPI manual quando houver mudanca REST;
- usar PRs e checks conforme `AGENTS.md`.

### Trilha T5 — Go-to-market

Ferramentas: B2B Messaging Workshop, Creative Production, Sales, Strapi e Next.js.

Entregaveis:

- homepage alinhada ao positioning;
- landing pages por segmento;
- sales deck e demo narrative;
- campanhas e assets;
- formularios e atribuicao de leads;
- playbook comercial inicial.

Strapi continua sendo a fonte editorial; Spring Boot continua dono de lead operacional, trial, billing e limites.

### Trilha T6 — Analytics e aprendizado continuo

Ferramentas: Data Analytics, Build Web Data Visualization, BuildBetter.ai.

Eventos candidatos:

- `signup_completed`;
- `tenant_created`;
- `employee_created`;
- `schedule_created`;
- `schedule_validated`;
- `schedule_published`;
- `swap_requested`;
- `trial_started`;
- `trial_converted`;
- `subscription_started`;
- `subscription_cancelled`;
- `ai_suggestion_requested`;
- `ai_suggestion_accepted`.

Antes de instrumentar cada evento, definir finalidade, minimizacao, tenant, retencao e se ha dado pessoal envolvido.

## Seguranca, LGPD e multi-tenant

As ferramentas externas nunca alteram as seguintes regras:

- tenant deve ser derivado do principal autenticado no backend;
- nenhum output de IA pode autorizar acesso ou efetivar operacao privilegiada sozinho;
- nao enviar secrets, tokens, chaves AWS, dumps de banco ou credenciais a ferramentas de design/marketing;
- dados de funcionarios/clientes devem ser minimizados, anonimizados ou substituidos por dados sinteticos quando possivel;
- qualquer integracao futura com dados reais exige avaliacao de suboperador, finalidade, retencao e controles LGPD;
- outputs de IA devem ser revisados antes de entrar em codigo, marketing, contratos ou documentacao regulatoria.

## Criterio para adicionar um novo plugin

Antes de adotar outra ferramenta, registrar:

1. problema real que ela resolve;
2. sobreposicao com ferramentas ja adotadas;
3. impacto arquitetural e operacional;
4. custo;
5. dados/permissoes necessarios;
6. risco de fornecedor e lock-in;
7. impacto LGPD e multi-tenant;
8. estrategia de saida;
9. metrica de sucesso.

Sem justificativa suficiente, preferir o conjunto atual e evitar proliferacao de ferramentas.
