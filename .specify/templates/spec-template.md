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
