# AppSec e Critérios de Aceite — Escala

Data de referência: 2026-09-07.

## Objetivo

Este documento define como o Escala trata segurança de aplicação e critérios de aceite para contratos técnicos, comunicações e regras de negócio. Ele complementa `AGENTS.md`, `docs/roadmap.md`, os documentos de arquitetura e as decisões de produto.

A regra central é simples: uma feature não está pronta apenas porque o happy path funciona. Ela deve demonstrar contrato explícito, autorização correta, isolamento multi-tenant, comportamento de erro, observabilidade/auditoria quando aplicável e testes proporcionais ao risco.

## 1. AppSec no ciclo de desenvolvimento

### Ferramenta prioritária

- **Codex Security:** ferramenta assistiva prioritária para scans, análise e investigação de segurança do código e da configuração.
- O uso do plugin não substitui revisão humana, CI, testes automatizados, scanners de dependência, secret scanning, threat modeling ou validação de autorização.
- Findings devem ser classificados por impacto, explorabilidade, superfície afetada e risco multi-tenant.

### Ferramentas complementares

- **GitHub/CI:** source of truth para PRs, checks obrigatórios e rastreabilidade.
- **Dependabot/alerts ou scanner equivalente:** dependências vulneráveis e supply chain.
- **Secret scanning:** segredos expostos em histórico, código e configuração.
- **SAST/CodeQL quando disponível:** análise estática de fluxos inseguros.
- **DAST em homolog quando justificável:** rotas públicas, autenticação, autorização e headers.
- **CertScore.ai Privacy Scanner:** útil apenas para auditoria pública de cookies, trackers e sinais de privacidade do site publicado; não substitui avaliação jurídica/LGPD.
- **Radar Lite:** opcional para DNS, email authentication e postura externa de domínio quando o Escala possuir domínio público de produção.
- **ArmorCodex:** opcional para governança de agentes e políticas de uso do Codex; não é scanner da aplicação.

Não adicionar SDKs dessas ferramentas ao runtime do Escala sem necessidade funcional, ADR e avaliação de segurança/LGPD.

## 2. Gates mínimos de segurança

Uma mudança de risco relevante não deve ser integrada enquanto houver finding crítico conhecido sem mitigação aceita.

### Gate SEC-01 — Autenticação e autorização

- endpoint privado exige identidade autenticada;
- autorização deve ser validada no backend, não apenas na UI;
- role/permissão deve ser explicitada no critério de aceite;
- IDOR deve ser testado para recursos por identificador;
- tentativa de acesso por usuário sem permissão deve falhar com resposta consistente e sem vazamento de detalhes.

### Gate SEC-02 — Multi-tenant

- tenant deve vir do principal/contexto server-side;
- nunca confiar em `tenantId/companyId` arbitrário vindo do cliente;
- queries tenant-bound devem incluir isolamento explícito;
- testes devem tentar acesso cross-tenant de leitura e escrita;
- logs/auditoria devem registrar tenant sem expor PII desnecessária.

### Gate SEC-03 — Entrada, saída e dados

- validar request no boundary;
- evitar mass assignment e bind direto de entidade persistente;
- DTOs de resposta devem expor somente campos necessários;
- uploads devem validar tipo, tamanho, nome, armazenamento e autorização;
- dados sensíveis devem ser minimizados em logs e erros.

### Gate SEC-04 — Sessão, cookies e frontend

- cookies de sessão devem usar atributos adequados (`HttpOnly`, `Secure` em produção e `SameSite` apropriado);
- CSP, HSTS, frame protection e políticas de origem devem ser compatíveis com o fluxo real;
- ações mutáveis devem considerar CSRF conforme o mecanismo de autenticação;
- conteúdo vindo do CMS não pode permitir XSS arbitrário.

### Gate SEC-05 — Supply chain e secrets

- nenhum segredo no repositório;
- dependências novas exigem justificativa e avaliação de manutenção/licença/risco;
- vulnerabilidade crítica/alta explorável exige correção, mitigação ou aceite formal de risco antes da produção;
- imagens Docker e lockfiles devem fazer parte da revisão de supply chain.

### Gate SEC-06 — Auditoria e observabilidade

- operações privilegiadas ou com impacto trabalhista/comercial devem ter rastreabilidade adequada;
- erros devem possuir correlation/request id quando possível;
- logs não devem conter senha, token, documento pessoal completo ou payload sensível sem necessidade;
- eventos de segurança relevantes devem ser distinguíveis de erro funcional comum.

## 3. Critérios de aceite para contratos técnicos

“Contrato” inclui REST, BFF, DTOs, eventos de domínio, mensagens assíncronas e integrações externas.

Toda mudança de contrato deve declarar:

1. consumidor e produtor;
2. método/rota ou nome do evento;
3. autenticação e autorização;
4. escopo de tenant;
5. request/schema de entrada;
6. response/schema de saída;
7. códigos de sucesso e erro;
8. validações e invariantes relevantes;
9. idempotência quando houver retry/pagamento/publicação/mensagem;
10. compatibilidade/backward compatibility ou estratégia de versão;
11. observabilidade e correlação;
12. testes de contrato/integracão correspondentes.

### Aceite REST/BFF

Uma mudança REST/BFF só é aceita quando:

- OpenAPI/documentação correspondente foi atualizada;
- frontend e backend concordam com nomes, tipos, nulabilidade e enumerações;
- erros previsíveis têm códigos e payload consistentes;
- autorização e tenant isolation foram testados;
- dados não necessários não aparecem na resposta;
- paginação/filtros/ordenação possuem limites e defaults explícitos quando aplicável;
- mudança breaking é evitada ou possui plano de migração/versionamento.

### Aceite de eventos/mensageria

Quando houver comunicação assíncrona:

- evento deve possuir nome e versão;
- produtor e consumidores devem ser conhecidos;
- retry e duplicidade devem ser considerados;
- consumidor deve ser idempotente quando necessário;
- falha de envio não pode corromper a transação principal;
- dead-letter/reprocessamento só deve ser adicionado quando houver justificativa operacional;
- payload deve respeitar minimização de dados e tenant.

## 4. Critérios de aceite para comunicações

Comunicação inclui email, notificação in-app, push, mensagens para gestores/colaboradores, mensagens de trial/billing e mensagens operacionais.

Toda comunicação deve definir:

- gatilho;
- destinatário e por que ele pode receber a mensagem;
- canal;
- conteúdo mínimo necessário;
- locale/idioma quando aplicável;
- CTA/destino;
- comportamento em retry/duplicidade;
- auditabilidade quando a comunicação tiver efeito relevante;
- tratamento de indisponibilidade do provedor.

### Regras de aceite

- não enviar informação de outro tenant;
- não revelar dado sensível além do necessário;
- links autenticados devem levar o usuário ao recurso correto e respeitar autorização ao abrir;
- mensagem não substitui validação backend;
- envio duplicado deve ser evitado ou tolerável;
- preferências de comunicação/opt-out devem ser respeitadas quando aplicável;
- mensagens legais/comerciais devem usar claims aprovados e não prometer conformidade não validada;
- falha no canal secundário não deve desfazer operação principal já confirmada, salvo regra explícita.

## 5. Critérios de aceite para regras de negócio

Toda regra de negócio relevante deve ter critérios de aceite verificáveis em linguagem de domínio e testes automatizados.

### Template mínimo

**Regra:** descrição em termos de negócio.

**Pré-condições:** estado necessário para executar.

**Atores autorizados:** quem pode executar.

**Tenant:** como o escopo é determinado.

**Happy path:** comportamento esperado.

**Casos de borda:** datas, limites, estados concorrentes, entradas vazias, timezone, etc.

**Invariantes:** o que nunca pode ocorrer.

**Erros de domínio:** códigos/mensagens esperados.

**Efeitos colaterais:** auditoria, notificação, evento, cobrança, consumo de IA.

**Idempotência/concorrência:** comportamento em retry ou duas ações simultâneas.

**Observabilidade:** logs/métricas/eventos úteis.

**Testes:** unitários, integração e E2E conforme risco.

### Exemplos de invariantes do Escala

- um colaborador não pode possuir duas atribuições incompatíveis no mesmo período;
- uma troca não pode ser efetivada sem os estados/aprovações exigidos;
- um gestor não pode operar recurso de tenant ao qual não pertence;
- publicação de escala deve respeitar as validações e ciência de alertas críticos definidas pelo domínio;
- limite de plano/IA deve ser aplicado no backend, nunca apenas na UI;
- webhook/billing não pode gerar cobrança/efeito duplicado por retry;
- auditoria append-only não pode ser alterada por fluxo normal da aplicação.

## 6. Definition of Ready

Uma issue está pronta para implementação quando, conforme aplicável:

- problema e valor estão claros;
- atores/personas estão identificados;
- critérios de aceite funcionais estão escritos;
- contrato técnico está definido ou impacto contratual declarado como inexistente;
- regras de negócio/invariantes estão explicitadas;
- impacto de segurança e multi-tenant foi avaliado;
- impacto LGPD/compliance foi avaliado;
- estados UX de loading/empty/error/disabled/responsivo foram considerados;
- dependências externas e riscos estão conhecidos.

## 7. Definition of Done

Uma issue só está concluída quando, conforme aplicável:

- critérios de aceite passam;
- testes unitários de domínio passam;
- testes de integração/contrato passam;
- tentativa cross-tenant e autorização negativa foram cobertas para recurso tenant-bound;
- lint/typecheck/build passam;
- backend inicia em Docker e health/OpenAPI são validados quando backend foi alterado;
- documentação/OpenAPI foi atualizada;
- findings críticos/altos relevantes de segurança foram tratados;
- logs/auditoria/telemetria necessários existem;
- sem secrets ou dados sensíveis indevidos no diff;
- UX aprovada inclui estados de erro, loading e acessibilidade quando aplicável;
- rollback ou estratégia de reversão é conhecida para mudanças de risco.

## 8. Severidade e tratamento de findings

- **Crítico:** bloqueia merge/release. Ex.: cross-tenant leak, auth bypass, secret válido exposto, execução remota explorável.
- **Alto:** normalmente bloqueia release; exige correção ou aceite formal de risco com mitigação e prazo.
- **Médio:** entra no backlog priorizado com contexto e prazo proporcional ao risco.
- **Baixo:** pode ser tratado como hardening/qualidade, sem mascarar dívida recorrente.

Finding de ferramenta nunca deve ser aceito ou rejeitado apenas pela severidade automática: validar explorabilidade e contexto real.

## 9. Uso do Codex Security

Usar prioritariamente:

- antes de release relevante;
- em mudanças de IAM, RBAC/ReBAC, multi-tenant, billing, uploads, sessões, IA com dados sensíveis e integrações externas;
- após mudança importante de dependências/framework;
- em investigação de incidente/finding;
- periodicamente para baseline de segurança.

Saída esperada:

1. finding;
2. evidência/arquivo/fluxo afetado;
3. impacto;
4. explorabilidade;
5. impacto multi-tenant;
6. recomendação;
7. teste de regressão;
8. decisão: corrigir, mitigar, aceitar risco ou falso positivo.

## 10. Regulatório

- LGPD deve ser avaliada para coleta, finalidade, minimização, retenção, compartilhamento e direitos do titular.
- Portaria 671 deve ser tratada apenas quando o fluxo realmente entrar no escopo técnico/regulatório de registro eletrônico de ponto; não usar alegação comercial de conformidade sem validação específica.
- Critérios jurídicos/comerciais que dependam de interpretação legal devem ser revisados por profissional qualificado antes de virar promessa contratual.