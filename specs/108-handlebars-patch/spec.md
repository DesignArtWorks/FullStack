# Especificação — correção transitiva do CMS

Spec: `108-handlebars-patch` | Issue: #108, recorte #99-F | Data: 2026-10-09
Dossiê: [interview.md](interview.md) | Plano: [plan.md](plan.md) | Tarefas: [tasks.md](tasks.md).
Estado: H1 aprovado antes do patch; artefatos completos registrados após revisão; H2/merge pendentes. Não declarar uma entrevista nova ou documentos preexistentes.

## Problema, ator, valor e origem

O mantenedor precisa remover o bloqueio de dependência vulnerável do CMS sem perder geração legítima nem enfraquecer os checks. Origem: PROV-HB001/002/003 no dossiê, pedido humano de CI verde e política SCA #108/#99-F. O patch não cria regra de negócio.

## Histórias priorizadas

### US-HB1 — dependência corrigida com compatibilidade (P1)

Como mantenedor, quero que a dependência de geração deixe de expor os dois comportamentos vulneráveis e mantenha a geração válida, para aprovar o gate de segurança com evidência.

- Dado um template tentando alcançar o construtor protegido, quando renderizado, então o construtor não é exposto.
- Dada AST malformada, quando compilada/precompilada, então há rejeição sem executar o marcador injetado.
- Dada entrada legítima, quando a geração é executada, então produz conteúdo e arquivo esperados.
- Falha/skip/cancelamento em check impede declarar aceite global.

## Regras de origem e requisitos

| Regra / fonte | Requisito | Responsável / história |
| --- | --- | --- |
| RN-SCA108-HB001, [GHSA-p8wg-vrv2-v86f](https://github.com/handlebars-lang/handlebars.js/security/advisories/GHSA-p8wg-vrv2-v86f), PROV-HB001/003 | FR-HB001: não expor Function.constructor por lookup permissivo | H1 Wemerson; US-HB1 |
| RN-SCA108-HB002, [GHSA-8r5x-fm3f-whwj](https://github.com/handlebars-lang/handlebars.js/security/advisories/GHSA-8r5x-fm3f-whwj), PROV-HB001/003 | FR-HB002: rejeitar AST malformada antes de executar código injetado | H1 Wemerson; US-HB1 |
| RN-SCA108-HB003, consumidores atuais e PROV-HB001 | FR-HB003: preservar geração legítima de conteúdo e arquivos | H1 Wemerson; US-HB1 |

## Aceite por regra, limites e bordas

| Campo | RN-HB001 / RN-HB002 / RN-HB003 (prefixo completo RN-SCA108) |
| --- | --- |
| Pré-condições | Dependência instalada; template/function para HB001; AST inválida para HB002; dados/templates válidos para HB003 |
| Atores autorizados | Mantenedor executa testes/scaffolding local; não modifica autenticação da aplicação |
| Tenant | N/A: tooling de geração sem recurso tenant-bound, query ou acesso a banco |
| Happy path | HB001 não revela construtor; HB002 rejeita AST; HB003 renderiza e gera arquivo correto |
| Bordas / invariantes | Métodos de protótipo permissivos; blockParams e literal depth; strings/AST válidas/helpers/partials/loops; não excluir teste por ser sintético |
| Erros | Rejeição da entrada inválida; falha de instalação/execução é erro de ambiente, não sucesso de segurança |
| Efeitos | Somente arquivos descartáveis no teste de geração; sem banco, auditoria operacional, rede ou credencial |
| Concorrência/idempotência | N/A para domínio; testes usam diretório temporário exclusivo; geração verificável em execução isolada |
| Observabilidade | Logs inertes dos testes e CI, sem segredos ou PII |
| Testes | Negativos HB001/HB002 e controle positivo HB003; testes cross-tenant N/A porque nenhum recurso de tenant muda |

## Critérios de sucesso

| ID | Critério / origem | Demonstração |
| --- | --- | --- |
| SC-HB001 | Construtor não exposto, FR-HB001 | Saída vazia; falha na versão anterior e aprovação na corrigida |
| SC-HB002 | AST inválida não executa marcador e é rejeitada, FR-HB002 | Negativos compile/precompile, duas classes |
| SC-HB003 | Conteúdo renderizado e arquivo legítimo criado, FR-HB003 | Templates/helpers/partials/loops, geração real e build |
| SC-HB004 | Oito checks success no head efetivo, PROV-HB001 | CI obrigatório, incluindo SCA/Secret Scan/Required Gate |

## Escopo, exclusões e suposições

Somente correção transitiva do CMS e documentação/testes necessários. Nenhuma mudança REST/BFF/OpenAPI, banco, autorização, tenant, UI ou comunicação operacional. LGPD: fixtures inertes sem dado pessoal/credencial. Não concluir exploração remota por leitura de CVE, nem rotação/#106/#99/main por este patch. H2 pendente; rollback não autoriza reintroduzir vulnerabilidade.

## Definition of Ready e limite temporal

Problema/ator/aceite, ausência de mudança contratual, dados/tenant/UX N/A justificados, dependências e riscos estão definidos acima e no plano. Escopo humano foi confirmado em Q-HB001 antes da alteração; dossiê/spec/tasks versionados foram acrescentados depois, conforme registro explícito. Esta regularização não prova cumprimento documental prévio. Aprovação H1 não é repetida; revisão H2 dos artefatos/resultados continua obrigatória antes de integrar.
