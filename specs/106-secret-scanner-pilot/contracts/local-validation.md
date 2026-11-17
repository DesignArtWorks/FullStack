# Contrato de diagnóstico local — US2 / #106

Contrato aprovado pelo usuário nesta sessão em 2026-11-17: “Aprovo o contrato proposto.” RN-SEC106-003, FR-006, T009/T010.
AB-001 resolvida pelo usuário nesta sessão: manter bloqueio do segredo;
inconclusivo não libera entrega. Não altera o gate histórico 0/42/2.

## Alvos e fontes

Configuração local privada, ignorada pelo Git, associa cada item a um ID opaco,
serviço, endereço loopback literal, porta, protocolo, usuário/database quando
aplicável, e fonte explícita. Nunca derivar destino do conteúdo de um .env.
Fontes: chave em arquivo .env ignorado ou chave em arquivo de commit Git
explicitamente selecionado; sem varredura indiscriminada de valores ou adivinhação
de usuário. Não executar/sourcing do .env, interpolação ou expansão de comandos.
Não passar valor secreto por argumento de processo nem gravá-lo em relatório.

Somente 127.0.0.1 ou ::1; rejeitar hostnames, destinos externos, redirects,
URL com credenciais/query/fragmento e proxies herdados. Alvo deve ser desenvolvimento
local autorizado. Não usar produção/homologação ou credenciais de provedores.

## Provas por serviço

- Spring: POST no endpoint de login existente, usuário conhecido; verificar
  resposta autenticada, descartar token; nunca testar escala/cadastro/mutação.
- Next/BFF: obter CSRF/cookie local e enviar login credentials; confirmar sessão
  retornada e efetuar logout local quando suportado. Não seguir redirect.
- Strapi: somente login administrativo local conhecido; identidade de usuário
  final deve continuar desativada. Descartar JWT; não criar usuário/conteúdo.
- PostgreSQL: cliente local, senha em ambiente do subprocesso, conexão loopback,
  transação read-only/SELECT 1; sem SQL de escrita ou senha na linha de comando.
- Redis: AUTH seguido de PING e QUIT; sem escrita/flush/config ou senha em argv.
- JWT_SECRET/NEXTAUTH_SECRET e demais signing keys: NÃO_VERIFICÁVEL por login;
  não forjar token, comparar chave com serviço externo nem inferir revogação.

## Estados e limites

ACESSO_CONFIRMADO: evidência específica do protocolo, não apenas HTTP 200.
ACESSO_NEGADO: rejeição explícita de autenticação (401/403 ou erro de senha do
protocolo). INCONCLUSIVO: indisponibilidade, timeout, rate limit, resposta ambígua,
ferramenta ausente ou erro do serviço. NÃO_VERIFICÁVEL: chave de assinatura ou
associação incompleta. Nenhum deles apaga finding histórico ou prova revogação.

Uma tentativa de autenticação por item, sem retries, timeout 5 segundos por
operação; máximo 20 itens por execução. Fonte inválida falha antes de conectar.
Somente relatórios ID/serviço/estado/código de motivo fixo; sem URL, username,
conteúdo HTTP, cookies, token, segredo ou erro bruto. Sessões/auditoria/rate limit
são efeitos possíveis; nenhuma operação de negócio é permitida.

Modo padrão: validar configuração sem autenticar. Execução exige flag explícita.
Testes usam exclusivamente valores sintéticos e servidores loopback isolados:
sucesso, negado, indisponível, redirect, erro, não vazamento e bloqueio externo.
A execução real depende do mapping local revisado; nunca preencher com suposições.

## Saída e entrega

Diagnóstico não é gate que libera credenciais: qualquer acesso confirmado exige
tratamento no inventário de rotação. Inconclusivo/não verificável permanece pendente.
Rotação local segue o marco existente: após #103/#106/#109 e antes do gate final
#99, com registro próprio; conclusão de código não comprova revogação.
Main e saneamento de histórico adicional pertencem à #128. Falha histórica de
governança permanece registrada, sem nova manutenção/bypass nesta entrega.

## Interface implementada

`python3 scripts/security/validate-local-credentials.py --repo /checkout --mapping .private/credentials.json`
valida sem conectar; `--execute` realiza o diagnóstico. Tanto o JSON quanto os
.envs devem estar ignorados e não rastreados no mesmo checkout. Não cadastrar
material real neste exemplo. Python 3 standard library; PostgreSQL requer `psql`
no PATH padrão do sistema, ausência retorna INCONCLUSIVO.

Formato do JSON: lista (1–20) de objetos com `id` (`item-001` etc.), `service`
(`spring`, `next`, `strapi`, `postgres`, `redis`, `signing-key`), `environment`
(`local-development`), `host` (literal loopback), `port`, `username` e `source`.
Postgres requer `database`; Spring/Next aceitam `companySlug` explícito.
`source`: `kind=env`, `path` relativo ignorado, `key`; ou `kind=git`, SHA completo
em `commit`, arquivo explícito em `path`, `key`. Chave única em linha `KEY=value`
ou `KEY: value`, sem interpolação. Signing keys não são lidas ou enviadas.

Saída: 2 configuração inválida; 3 acesso confirmado/inconclusivo/não verificável;
0 configuração validada sem execução ou todas as tentativas negadas.
Sempre `releases_findings=false`: nem exit 0 revoga ou libera finding.
PostgreSQL SELECT 1 comprova conexão autorizada, mas não que a senha foi exigida
(se pg_hba usa trust); resultado não demonstra validade criptográfica da senha.
Tokens Spring/Strapi são descartados: não há endpoint genérico de revogação
administrativa usado aqui. Next tenta logout e reporta falha de limpeza como
INCONCLUSIVO. Auditoria/sessão no servidor pode permanecer.
