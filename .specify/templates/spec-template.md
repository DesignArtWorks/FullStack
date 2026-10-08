# Especificação — <nome>

Spec: `<numero>-<slug>` | Issue: #<numero> | Data: <data> | Estado: proposta

## Problema, ator, valor e origem

<Pedido e fontes locais. Distinguir decisão aprovada, estado atual e objetivo futuro.>

## Histórias priorizadas

### US1 — <jornada> (P1)

Como <ator>, quero <comportamento> para <valor>.

- Dado <precondição>, quando <ação>, então <resultado observável>.
- Cenário negativo / erro / limite: <resultado esperado>.

## Regras de origem

| ID estável | Regra aprovada | Fonte exata / seção | Responsável / decisão |
| --- | --- | --- | --- |
| RN-<contexto>-001 | <regra> | <arquivo/issue/decisão> | <referência> |

Preservar IDs existentes; usar RC/RD/RN quando adequado. Não inventar limite ou direito como suposição. Marcar `[NEEDS CLARIFICATION]`, indicar responsável e bloquear o trecho dependente quando faltar decisão.

### Aceite por regra de negócio — repetir para cada ID

| Campo obrigatório | Definição aprovada / evidência |
| --- | --- |
| ID e origem | <regra, fonte, responsável, FR/US associados> |
| Pré-condições | <estado e dados necessários> |
| Atores autorizados | <papéis e autorização por recurso> |
| Tenant | <classificação e origem confiável do escopo; exceções explícitas> |
| Happy path | <entrada/ação/saída observável> |
| Bordas e invariantes | <limites exatos, datas, ausências e proibições> |
| Erros de domínio | <condições e comportamento esperado> |
| Efeitos colaterais | <persistência, auditoria, eventos e comunicações> |
| Concorrência e idempotência | <conflito, retry e duplicidade; ou N/A justificado> |
| Observabilidade | <eventos/correlação sem dados desnecessários> |
| Testes / demonstração | <cenários positivos, negativos e cross-tenant pertinentes> |

Não marcar a Definition of Ready concluída com campo aplicável ausente. N/A exige justificativa por campo.

## Requisitos

| ID | O sistema deve... | Regra de origem | História |
| --- | --- | --- | --- |
| FR-001 | <comportamento sem tecnologia> | RN-<contexto>-001 | US1 |

## Entidades, limites e casos de borda

<Vocabulário, relações, limites exatos aprovados, dados ausentes, conflito, duplicidade, datas/fusos, autorização e tenant.>

## Critérios de sucesso

| ID | Critério mensurável sem tecnologia | Origem | Como demonstrar |
| --- | --- | --- | --- |
| SC-001 | <resultado> | FR-001 / regra | <teste/medida> |

## Escopo, exclusões e suposições

<Não criar métricas sem fonte. Hipóteses técnicas vão no plano; hipóteses de negócio que mudam saída/direito exigem decisão.>

## Definition of Ready

- [ ] Problema/ator/valor e aceite definidos.
- [ ] Regras/invariantes e casos de borda aprovados.
- [ ] Contrato ou ausência de mudança identificado.
- [ ] Autorização, tenant, dados/LGPD e estados UX identificados.
- [ ] Dependências, riscos e decisões pendentes explícitos.
