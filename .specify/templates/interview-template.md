# Dossiê da entrevista socrática — <nome>

Spec/issue: <ID> | Interlocutor: <nome/papel> | Entrevistador: <nome>
Data/revisão: <data/versão> | Estado: EM_ENTREVISTA
Escopo autorizado / modo de execução: <referência humana identificável>

## 1. As três canônicas

| Canônica | Resposta precisa | Origem / confirmação | Estado |
| --- | --- | --- | --- |
| O que estamos construindo | <comportamento e limites> | <resposta/decisão> | PENDING |
| Para quem | <ator/público e contexto> | <resposta/decisão> | PENDING |
| Critério de sucesso testável | <resultado e demonstração aprovada> | <resposta/decisão> | PENDING |

Critérios de saída das fases: enquadramento <evidência>; exploração <evidência>; aprofundamento <evidência>; ampliação <evidência>; síntese <evidência>.
Validação humana do entendimento: <autor/data/trecho>; pendências bloqueadoras: <IDs ou nenhuma comprovada>.
Não declarar VALIDADA apenas porque foi solicitado fechar a entrevista.

## 2. Registro de proveniência

| ID | Afirmação | Classe | Fonte exata / resposta / data | Responsável | FR/RN afetados |
| --- | --- | --- | --- | --- | --- |
| PROV-001 | <afirmação> | [PRESSUPOSTO] | <origem/limite> | <nome> | <IDs> |

Classes: [FATO], [EVIDÊNCIA], [PRESSUPOSTO], [DECISÃO], [RESTRIÇÃO], [RISCO], [ABERTA], [CONFLITO]. IDs locais à spec; citar <spec>:PROV-001 fora dela. Preservar fonte e histórico quando uma classificação mudar.

| Pergunta / fase / categoria | Resposta humana real | Origem | Síntese a cada quatro respostas |
| --- | --- | --- | --- |
| Q001 / enquadramento / esclarecimento | <resposta ou pendente> | <turno/documento> | <quando pertinente> |

Categorias examinadas: esclarecimento <QID>; pressupostos <QID>; evidências <QID>; implicações <QID>; perspectivas alternativas <QID>; meta-pensamento <QID>.
Não preencher com diálogo fictício. Referências anteriores podem ser reutilizadas com sua origem; revisão do escopo deve ser explícita.

## 3. Termos quantificados — antes e depois

| ID / termo original | Comportamento / número / exemplo aprovado | Fonte | Limite / estado |
| --- | --- | --- | --- |
| PROV-<ID> / <vago> | <definição ou ainda aberta> | <resposta> | <escopo> |

Não criar meta para completar a tabela. Sem medida aprovada, manter ABERTA e identificar a dependência.

## 4. Riscos examinados e perspectivas alternativas

| ID / cenário ou perspectiva | Implicação observável | Evidência ou pressuposto | Decisão / dono / encaminhamento |
| --- | --- | --- | --- |
| PROV-<ID> / <falha> | <impacto> | <origem> | <registro> |
| PROV-<ID> / <outra perspectiva> | <consequência> | <origem> | <registro> |

Considerar, conforme escopo: tenant, autorização, LGPD, fronteiras hexagonais, dados ausentes, limites exatos, concorrência, retry, falhas externas e prazo/cobertura sem escopo. Não inventar risco comprovado.

## 5. Perguntas abertas com dono

| ID | Pergunta em aberto | Dono | Bloqueia qual recorte | Critério para resolver / referência |
| --- | --- | --- | --- | --- |
| PROV-<ID> | <uma dúvida> | <nome ou responsável a designar> | <FR/tarefa> | <evidência/decisão esperada> |

Resultado do gate pré-implementação: PENDING.
Motivo e escopo liberado: <somente após evidência/validação humana; não inferir de timeout>.
Próximo passo: <continuar entrevista ou, após validação, sair do papel e propor/especificar>.
