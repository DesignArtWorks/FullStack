# Resiliencia, observabilidade e resposta a incidentes

Data de referencia: 2026-09-07.

## Objetivo

Definir como o Escala deve detectar, localizar, conter, mitigar, recuperar e aprender com falhas em frontend, BFF, backend, infraestrutura e PostgreSQL.

A meta nao e apenas registrar erros. A plataforma deve responder rapidamente a perguntas como:

- o que falhou;
- quando falhou;
- onde falhou;
- qual request, usuario/ator e tenant foram afetados, sem vazar dados sensiveis;
- qual dependencia participou da falha;
- qual versao/deploy introduziu ou ampliou o problema;
- qual foi o impacto funcional e comercial;
- como conter o problema sem ampliar o incidente;
- como recuperar o servico;
- como impedir recorrencia.

## Principios

1. Observabilidade deve ser desenhada junto da feature, nao adicionada somente apos incidentes.
2. Logs, metricas e traces devem ser correlacionaveis.
3. Falhas devem ser contidas no menor dominio possivel.
4. Sistemas secundarios nao devem derrubar o fluxo principal sem regra de negocio que justifique isso.
5. Retry sem idempotencia e limite pode piorar incidentes; usar apenas com criterio.
6. Nenhum log ou trace pode virar canal de vazamento LGPD ou cross-tenant.
7. Health check nao substitui readiness, liveness, SLO nem monitoramento funcional.
8. Incidentes relevantes geram postmortem sem busca de culpados e com acoes rastreaveis.
9. Correcoes emergenciais continuam sujeitas a tenant isolation, seguranca e rollback.
10. A telemetria deve ser proporcional ao risco e ao custo operacional.

## Arquitetura alvo de observabilidade

```text
Browser / Next.js
      |
      | traceparent / correlation-id
      v
Next.js / BFF
      |
      v
Spring Boot
      |
      +--> PostgreSQL
      +--> Strapi
      +--> email/notificacoes
      +--> IA/provedores externos

Todos os componentes
      |
      v
OpenTelemetry Collector
      |
      +--> Prometheus / metricas
      +--> Loki / logs
      +--> Tempo / traces
      v
Grafana / dashboards / alertas
```

Para AWS, a topologia pode evoluir gradualmente para servicos gerenciados e/ou CloudWatch, sem alterar os principios de correlacao e SLO.

## Identificadores de correlacao

Cada request relevante deve possuir identificadores consistentes:

- `trace_id`: trace distribuido;
- `span_id`: etapa atual do trace;
- `correlation_id`: identificador funcional/operacional quando necessario;
- `request_id`: request HTTP individual quando separado do trace;
- `tenant_id` ou identificador interno equivalente: somente quando permitido pela politica de logs e nunca vindo como autoridade do cliente;
- `actor_id`: preferir identificador tecnico interno, nao email/CPF;
- `operation`: nome estavel do caso de uso/endpoint;
- `release/version`: versao do software em execucao.

O frontend/BFF deve propagar `traceparent`/correlation id para o Spring Boot. O Spring Boot deve preservar a correlacao ao acessar banco e dependencias externas.

## Logs estruturados

Logs de producao devem ser estruturados e pesquisaveis, preferencialmente JSON.

Campos recomendados:

- timestamp UTC;
- severity;
- service;
- environment;
- version;
- trace_id;
- request_id/correlation_id;
- operation;
- route/template, evitando cardinalidade por URL livre;
- result/status;
- duration_ms quando aplicavel;
- error.type/error.code;
- dependency quando aplicavel.

### Dados proibidos ou fortemente restritos

Nao registrar por padrao:

- senha;
- JWT completo;
- refresh token;
- API key;
- cookie de sessao;
- Authorization header;
- CPF/documento pessoal;
- dados de saude/atestados;
- geolocalizacao precisa sem justificativa;
- payload integral de formulario/DTO;
- dados de outro tenant;
- connection strings com credenciais.

Quando dado pessoal for indispensavel para investigacao, aplicar minimizacao, mascaramento/pseudonimizacao, controle de acesso e retencao.

## Frontend e BFF

### Contencao de erros

- usar `error.tsx`/Error Boundaries por segmento/feature relevante no App Router;
- diferenciar erro recuperavel, indisponibilidade temporaria, permissao e erro de validacao;
- impedir que um widget ou painel secundario derrube toda a pagina quando puder degradar isoladamente;
- oferecer retry manual apenas para operacoes seguras;
- manter estados `loading`, `empty`, `error`, `offline/degraded` e `permission denied` quando aplicavel;
- nunca exibir stack trace, SQL, detalhes internos ou tokens ao usuario;
- associar erro apresentado ao usuario a um `errorId`/correlation id seguro para suporte.

### Telemetria frontend

Medir no minimo:

- erros JavaScript nao tratados;
- falhas de chamadas BFF;
- Web Vitals relevantes;
- latencia percebida por fluxo critico;
- taxa de falha em login, cadastro, publicacao de escala e check-in;
- versao/release que gerou o erro.

Nunca enviar conteudo sensivel de formularios ou estado privado indiscriminadamente para ferramentas de telemetry/session replay.

## Backend Spring Boot

### Tratamento consistente

- usar tratamento global de excecoes para converter falhas em contrato de erro estavel;
- separar erros de dominio, validacao, autorizacao, conflito, dependencia externa e erro interno;
- nao devolver mensagem de exception interna diretamente ao cliente;
- gerar correlation/trace id pesquisavel;
- registrar stack trace no servidor somente quando util e conforme severidade;
- evitar `catch (Exception)` que apenas engole erro ou retorna sucesso falso.

### Contencao de dependencia

Quando uma dependencia externa falhar:

- aplicar timeout explicito;
- retry apenas para falhas transientes e operacoes idempotentes/seguras;
- usar backoff e limite de tentativas;
- evitar retry sincronizado em cascata;
- considerar circuit breaker somente onde a dependencia e o volume justificarem;
- aplicar bulkhead/isolamento apenas quando houver evidencia de esgotamento compartilhado;
- definir fallback funcional somente se nao violar regra de negocio.

Bibliotecas de resiliencia como Resilience4j devem ser adotadas apenas onde houver caso de uso comprovado, evitando decoracao generalizada de todos os endpoints.

### Transacoes e efeitos colaterais

- operacao principal deve permanecer atomica no PostgreSQL quando necessario;
- notificacao/email nao deve corromper commit principal, salvo requisito explicito;
- usar outbox/eventos quando for necessario garantir entrega apos commit;
- usar idempotency key em operacoes expostas a repeticao com impacto financeiro/irreversivel;
- preservar auditabilidade de falhas e retries relevantes.

## PostgreSQL

O banco deve possuir observabilidade propria. `pg_isready` cobre somente uma pergunta limitada: se o servidor aceita conexoes.

Monitorar:

- disponibilidade;
- conexoes usadas/maximas;
- Hikari active/idle/pending;
- tempo de query e slow queries;
- locks e deadlocks;
- transacoes longas;
- cache hit ratio quando util;
- tamanho das tabelas/indexes;
- crescimento de `audit_logs` e `time_records`;
- disco livre;
- WAL/checkpoints;
- replicas/replication lag quando existirem;
- falhas de backup e restore;
- saturacao de CPU/IO/memoria em RDS.

### Contencao de incidentes de banco

- nao reiniciar banco como primeira resposta sem diagnostico;
- identificar query/lock/transacao causadora antes de kill quando possivel;
- limitar pool da aplicacao para nao exaurir conexoes;
- cancelar query runaway quando seguro;
- usar `statement_timeout`/timeouts apropriados em producao;
- impedir migrations destrutivas sem plano/backup;
- ter rollback ou forward-fix para migrations;
- testar restauracao de backup, nao apenas existencia do snapshot.

## Health, readiness e liveness

Separar conceitos:

- **liveness:** processo esta vivo e nao travado de forma irrecuperavel;
- **readiness:** instancia pode receber trafego com seguranca;
- **health funcional:** dependencia/fluxo relevante esta operacional;
- **synthetic check:** simula fluxo de usuario/negocio controlado.

`/actuator/health` deve continuar protegido conforme arquitetura de producao. Detalhes internos de dependencias nao devem ser expostos publicamente.

## SLOs e indicadores iniciais

SLIs/SLOs devem ser definidos por jornada critica, nao apenas por servidor.

Exemplos iniciais:

- disponibilidade de login;
- disponibilidade de leitura da escala publicada;
- disponibilidade do editor de escala;
- taxa de sucesso de publicacao de escala;
- taxa de sucesso de check-in;
- p95/p99 de endpoints criticos;
- 5xx por rota;
- erros de dependencia;
- filas/outbox pendentes quando existirem.

Baselines ja documentados no projeto nao devem ser tratados como SLA comercial ate validacao em topologia de producao.

## Alertas

Alertas precisam ser acionaveis. Evitar alerta sem dono ou que dispara constantemente sem necessidade.

Baseline inicial recomendado, sujeito a tuning:

- 5xx > 1% por 5 minutos em rota critica;
- p95 de leitura > 1s por 10 minutos;
- p95 de escrita > 3s por 10 minutos;
- Hikari pending > 0 de forma sustentada;
- memoria JVM > 85% do limite;
- CPU backend > 80% por periodo sustentado;
- PostgreSQL conexoes > 80% do limite;
- locks/deadlocks anormais;
- disco > 80%;
- backup falhou;
- synthetic login/publicacao falhou repetidamente;
- aumento abrupto de erros frontend apos release.

## Classificacao de incidentes

### SEV-1 — Critico

Exemplos:

- vazamento cross-tenant;
- auth bypass exploravel;
- indisponibilidade ampla de fluxos criticos;
- perda/corrupcao de dados;
- falha de banco sem caminho de recuperacao imediato;
- publicacao incorreta de escala em massa por defeito sistemico.

Tratamento: contenção imediata, prioridade maxima, comunicacao interna centralizada, preservar evidencias e avaliar obrigacoes LGPD quando houver incidente de dados.

### SEV-2 — Alto

Exemplos:

- feature critica degradada para grupo relevante de clientes;
- latencia severa sustentada;
- notificacoes essenciais atrasadas sem perda de regra principal;
- dependencia externa causando falha recorrente.

### SEV-3 — Moderado

- funcionalidade secundaria indisponivel;
- erro com workaround claro;
- degradacao restrita sem risco de integridade.

### SEV-4 — Baixo

- defeito visual ou operacional de baixo impacto;
- alerta preventivo sem impacto ao cliente.

## Ciclo de resposta a incidentes

```text
DETECTAR
   -> TRIAR E CLASSIFICAR
   -> LOCALIZAR
   -> CONTER
   -> MITIGAR
   -> RECUPERAR
   -> VALIDAR
   -> COMUNICAR
   -> POSTMORTEM
   -> ACOES PREVENTIVAS
```

### 1. Detectar

Origem pode ser alerta, cliente, suporte, CI, scanner, synthetic check ou observacao humana.

### 2. Triar

Registrar:

- inicio estimado;
- severidade;
- impacto;
- servicos/tenants potencialmente afetados;
- versao/deploy recente;
- incident commander quando SEV-1/SEV-2.

### 3. Localizar

Usar trace/correlation id para navegar:

frontend -> BFF -> backend -> banco/dependencia.

Comparar deploys, taxa de erro, latencia, logs e queries.

### 4. Conter

Preferir a menor mudanca reversivel que reduza impacto:

- rollback de release;
- feature flag/desabilitar funcionalidade nao critica;
- bloquear endpoint vulneravel;
- retirar instancia ruim do trafego;
- pausar job/consumer defeituoso;
- rate limit temporario;
- isolar dependencia.

Nao usar feature flag para contornar autorizacao ou tenant isolation.

### 5. Mitigar e recuperar

Aplicar fix, rollback, restore ou ajuste operacional. Validar por health, synthetic check e metrica de negocio.

### 6. Comunicar

Para incidente que afeta clientes, manter mensagem factual: impacto conhecido, status e proximo ponto de atualizacao. Nao especular causa antes de evidencia.

### 7. Postmortem

Obrigatorio para SEV-1 e normalmente SEV-2.

Registrar:

- resumo;
- timeline;
- impacto;
- deteccao;
- causa raiz e fatores contribuintes;
- porque controles existentes nao evitaram/detectaram antes;
- o que funcionou e o que falhou na resposta;
- acoes corretivas com responsavel/prioridade;
- testes/gates adicionados;
- necessidade de atualizar runbook/ADR.

## Runbooks minimos

Criar e manter, no minimo:

1. frontend com aumento de erro apos deploy;
2. BFF/backend com aumento de 5xx;
3. backend sem readiness;
4. pool Hikari esgotado;
5. PostgreSQL conexoes esgotadas;
6. slow query/lock/deadlock;
7. disco/WAL em crescimento critico;
8. backup falhou / restore necessario;
9. dependencia externa indisponivel;
10. fila/outbox acumulando quando existir;
11. incidente de autenticacao/autorizacao;
12. suspeita de vazamento cross-tenant;
13. release ruim e rollback;
14. degradacao por CPU/memoria.

Cada runbook deve conter sinais, consultas/comandos seguros, passos de contencao, rollback, criterios de recuperacao e escalonamento.

## Testes de resiliencia

Executar progressivamente em ambiente isolado:

- backend indisponivel enquanto frontend continua servindo erro controlado;
- PostgreSQL temporariamente indisponivel;
- timeout/latencia entre backend e banco;
- dependencia externa lenta/fora;
- pool JDBC limitado;
- CPU/memoria degradadas;
- query lenta/lock controlado;
- restart de instancia durante trafego;
- retry/idempotencia sob duplicidade;
- rollback de deploy;
- restore de backup em ambiente de teste.

Nao executar testes destrutivos contra dados reais de producao sem plano e controles explicitos.

## Seguranca e multi-tenant na observabilidade

- tenant deve ser derivado do contexto autenticado, nunca de header livre usado como autoridade;
- filtros e dashboards operacionais devem impedir acesso indevido a telemetria de outros tenants quando expostos a clientes;
- stack traces/logs internos devem ser restritos ao time autorizado;
- correlation id pode ser mostrado ao usuario, desde que nao carregue informacao sensivel;
- incidentes com suspeita de dados pessoais exigem preservacao de evidencias e avaliacao LGPD;
- logs de auditoria e logs tecnicos possuem finalidades distintas e nao devem ser confundidos.

## Roadmap de implementacao

### O0 — Baseline imediato

- padronizar correlation/request id frontend/BFF/backend;
- padronizar contrato de erro e error id;
- criar Error Boundaries/fallbacks nos fluxos criticos;
- definir logs estruturados e redaction;
- separar liveness/readiness/health;
- definir severidades e template de incidente/postmortem.

### O1 — Metricas e dashboards

- habilitar metricas internas seguras do Spring/Micrometer;
- monitorar JVM, Tomcat, Hikari e endpoints;
- adicionar Postgres Exporter;
- dashboards Grafana para app e banco;
- primeiros SLOs por jornadas criticas.

### O2 — Tracing ponta a ponta

- OpenTelemetry no Next.js/BFF;
- OpenTelemetry no Spring Boot;
- Collector central;
- traces no Tempo ou backend equivalente;
- correlacao com logs e release.

### O3 — Alerting e incident response

- alertas acionaveis;
- rotas de escalonamento;
- runbooks;
- simulacao/tabletop de SEV-1/SEV-2;
- postmortem e tracking de acoes.

### O4 — Resiliencia avancada por evidencia

- timeouts por dependencia;
- idempotencia;
- outbox quando necessario;
- circuit breaker/bulkhead apenas em pontos comprovados;
- testes de falha controlados;
- validacao periodica de backup/restore.

## Definition of Done adicional

Feature ou integracao relevante nao deve ser concluida sem avaliar, conforme risco:

- como o erro sera detectado;
- qual correlation/trace id existira;
- quais logs/metricas/traces sao necessarios;
- quais dados nao podem ser logados;
- como dependencia falha e qual o timeout;
- se retry e seguro/idempotente;
- como a falha e contida;
- como suporte/operação identifica o incidente;
- como rollback/recuperacao sera realizado;
- quais alertas/runbooks precisam ser atualizados.
