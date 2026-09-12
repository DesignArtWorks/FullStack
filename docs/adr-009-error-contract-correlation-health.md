# ADR-009 — Contrato de erros, correlação e health O0

## Decisão

O boundary `Browser -> Next.js/BFF -> Spring Boot` usa `X-Correlation-ID`. O BFF preserva um identificador sintaticamente seguro recebido ou gera UUID; o Spring valida novamente, propaga na resposta e inclui o valor no MDC. O identificador nunca codifica usuário, email, tenant ou outro dado pessoal e nunca participa de autorização.

Erros públicos usam `{ code, message, status, errorId, correlationId }`. `code` é estável para o frontend; `message` é segura para exibição; `errorId` identifica a ocorrência para suporte. Exceções inesperadas são logadas internamente com stack trace e retornam mensagem genérica. Tokens, cookies, headers de autorização, SQL e payloads sensíveis não são logados.

`spring-boot-starter-validation` é a única dependência adicionada: o starter oficial é necessário para validar DTOs e constraints de método e mapear `MethodArgumentNotValidException`/`ConstraintViolationException` no mesmo contrato público.

## Responsabilidades de health

- `/actuator/health/liveness`: processo Spring vivo, sem dependência externa transitória.
- `/actuator/health/readiness`: dependências necessárias (`db` e `redis`) prontas para tráfego.
- `/actuator/health`: resumo compatível com Compose.
- `/api/health`: processo Next.js/BFF disponível, sem tratar HTML como health.
- PostgreSQL mantém `pg_isready`; ele não substitui readiness da aplicação.

Detalhes dos componentes de health nunca são expostos. Apenas endpoints de health são públicos; métricas continuam protegidas. O rollback consiste em reverter o commit da issue, mantendo `/actuator/health` compatível.
