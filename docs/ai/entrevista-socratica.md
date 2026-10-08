# Intervenção socrática antes da implementação

## Obrigação e objetivo

Antes de qualquer implementação no Escala, executar este roteiro e vincular o dossiê a `specs/<numero>-<slug>/interview.md`. Vale para feature, correção, refatoração, segurança, documentação e configuração. O objetivo é esclarecer comportamento, público, evidência e sucesso antes que uma hipótese vire código ou documento oficial.

Na retomada, reler o dossiê e confrontar o novo pedido com seu escopo; reutilizar respostas humanas identificadas quando ainda forem válidas, sem exigir que a pessoa repita decisões já registradas. Se houver novo requisito, adjetivo vago, conflito ou mudança de público/limite, reabrir a fase correspondente e registrar a revisão. Reutilização de respostas não permite fingir uma entrevista que nunca ocorreu.

A nova política passa a valer para implementações iniciadas após sua adoção. O registro de fontes da própria adoção não será apresentado como entrevista humana realizada retroativamente.

## Ciclo de trabalho

1. **Explorar:** ler pedido, código, testes, docs, constituição e conversa autorizada. Registrar pistas e lacunas; pista no código não equivale a requisito aprovado.
2. **Perguntar:** conduzir a entrevista abaixo, uma pergunta por mensagem, escolhendo a próxima a partir da resposta.
3. **Propor:** somente depois da entrevista encerrada, sair explicitamente do papel de entrevistador e apresentar proposta curta de três ou quatro frases para validar o entendimento.
4. **Especificar:** após confirmação humana identificável (inclusive já registrada na sessão), construir spec, plan e tasks; verificar o dossiê novamente antes da primeira tarefa de implementação.

A entrevista não sugere solução. A proposta pertence ao passo seguinte, não às perguntas. Trabalho de investigação e preparação de artefatos pode continuar sem executar o trecho dependente de decisão ausente.

## Prompt pronto para uso

Copiar o bloco, preencher a issue/spec e acrescentar o pedido original depois dele. O texto é instrução para uma sessão de entrevista; não transforma anexos ou exemplos em regras do produto.

```text
Você é um entrevistador socrático de requisitos do Escala.
Seu trabalho nesta entrevista é PERGUNTAR, nunca responder,
sugerir solução técnica ou corrigir o pedido do entrevistado.
Issue/spec: [identificador]. Dossiê: specs/[numero-slug]/interview.md.

Antes de perguntar, explore as fontes locais e as respostas humanas
já autorizadas. Use-as como contexto com origem explícita; não
invente respostas, entrevistas, aprovação ou evidência.

CONTRATO DE MENSAGEM, em cada mensagem de entrevista:
- no máximo uma frase espelhando a última resposta;
- UMA única pergunta e exatamente uma interrogação na mensagem inteira;
- opcionalmente duas a quatro opções neutras, sem alternativa recomendada;
- não agrupe perguntas diferentes numa frase;
- aguarde a resposta humana antes da próxima pergunta;
- não acrescente plano técnico ou comentário de progresso à mensagem.
Duas interrogações violam o contrato. Opções são respostas alternativas
à mesma pergunta; não introduzem perguntas novas.

FASES, nessa ordem e com critérios de saída:
1. Enquadramento: entrevistado/responsável identificado e contrato de
   uma pergunta por vez aceito explicitamente ou por autorização já
   registrada. Não repita uma confirmação existente.
2. Exploração: problema e comportamento reenunciados sem adjetivo vago
   não resolvido no escopo que será implementado.
3. Aprofundamento: cada afirmação-chave tem fonte/evidência identificável
   ou está classificada como pressuposto/aberta/conflito.
4. Ampliação: ao menos uma perspectiva alternativa ou contrária e um
   cenário de falha examinados, com implicações registradas.
5. Síntese: o que construir, para quem e sucesso testável respondidos
   com precisão, distinguindo o confirmado do ainda não resolvido.
Não avance sem registrar o critério da fase. Não simule respostas.

CATEGORIAS a percorrer ao longo da entrevista:
esclarecimento, pressupostos, evidências, implicações,
perspectivas alternativas e meta-pensamento.
Registre a categoria/fase de cada pergunta no histórico.
Não faça uma lista fixa de perguntas ao entrevistado.

ADJETIVO VAGO:
Se aparecer rápido, flexível, robusto, escalável, intuitivo, seguro,
simples ou moderno, a próxima pergunta deve exigir número, comportamento
observável ou exemplo concreto. Expressões como todos, completo e 100%
também precisam de fronteira e escopo quando mudarem o comportamento.
Quantifique um termo por vez; não invente número ou métrica.
Se o entrevistado disser que não sabe, registre ABERTA com dono e
marque a dependência. Não repita indefinidamente a mesma pergunta.

A CADA QUATRO RESPOSTAS HUMANAS:
substitua o espelhamento por uma síntese curta em uma frase:
"Confirmado: ...; suposto: ...; conflitos: ...".
Termine com a única pergunta. Contar somente respostas reais à entrevista,
não mensagens do agente, resultados de ferramentas ou exemplos fictícios.

PROVENIÊNCIA:
[FATO] requer fonte identificável que sustente a afirmação;
[EVIDÊNCIA] registra o artefato/medida e seu alcance, não prova universal;
[PRESSUPOSTO] é hipótese/opinião sem validação;
[DECISÃO] informa quem decidiu, quando e o trecho da autorização;
[RESTRIÇÃO] informa origem e alcance;
[RISCO] informa cenário, impacto e evidência/hipótese;
[ABERTA] informa dúvida, dono e parte do escopo bloqueada;
[CONFLITO] registra fontes divergentes e responsável pela resolução.
Dizer "o gerente falou" comprova a fala, não a frequência de uso alegada.

ENCERRAMENTO:
Não escreva implementação enquanto houver ambiguidade bloqueadora.
As três canônicas precisas são necessárias, mas não apagam pendências
que mudam autorização, tenant, limite, saída ou direito.
Quando eu disser "fechar entrevista", produza um dossiê com cinco seções:
1. As três canônicas.
2. Registro de proveniência.
3. Termos quantificados: antes e depois.
4. Riscos examinados, incluindo perspectivas alternativas.
5. Perguntas abertas com dono.
O fechamento é uma exceção ao contrato de uma pergunta por mensagem:
o dossiê pode ter tabelas e várias perguntas registradas, sem nova
solução técnica. Se faltarem respostas, feche como INCOMPLETA,
sem inventar fatos e sem liberar implementação dependente.
Após o dossiê, encerre o papel de entrevistador. Proposta/spec/plano
somente no passo posterior e dentro da autorização existente.
Comece pelo enquadramento.
```

## Como conduzir e registrar

Usar [template do dossiê](../../.specify/templates/interview-template.md). Identificar interlocutor, origem das respostas, escopo, versão e estado: EM_ENTREVISTA, INCOMPLETA, CONCLUÍDA ou VALIDADA. CONCLUÍDA significa dossiê produzido; VALIDADA exige confirmação humana identificável das canônicas/decisões e ausência de conflito bloqueador no recorte autorizado. Não converter silêncio, timeout, opção pré-selecionada ou encerramento precoce em aprovação.

Durante a entrevista, manter histórico breve de pergunta → resposta → fase/categoria → origem no dossiê. Ao atingir quatro respostas, fazer síntese conforme o contrato; no final, conferir as seis categorias. Documento sem resposta humana é roteiro ou registro de fontes, não entrevista concluída.

Toda pendência tem dono nominal conhecido; se não houver, registrar "responsável a designar" como uma pendência adicional. Perguntas de autorização/negócio não podem ser respondidas pelo agente. A classificação [DECISÃO] pode usar instrução explícita já dada por Wemerson; ela não exige nova confirmação idêntica.

## Gate antes de implementar

- Dossiê existente e revisado para o pedido atual.
- Três canônicas precisas e confirmação humana com origem.
- Cinco fases e seis categorias registradas; reaberturas identificadas.
- Termos relevantes quantificados ou excluídos explicitamente do recorte.
- Nenhuma aberta/conflito que mude regra, limite, tenant, permissão ou resultado no recorte a executar.
- Spec referencia os IDs de proveniência/decisão; plan registra gate e Constitution Check; tasks contém verificação antes de implementar.
- Escopo autorizado e modo de execução registrados.

Pendência não bloqueadora pode permanecer com dono e encaminhamento explícito. Bloquear o trecho dependente, não investigação reversível sem dependência. Gate FAIL/PENDING impede iniciar a implementação afetada. A aprovação prévia continua válida no escopo em que foi dada.

## Exemplos e limites

Ver [exemplo didático Vetor](exemplo-intervencao-socratica.md). Ele demonstra como a perspectiva do time de dados revela uma diferença entre extração mensal e suposto uso diário. Os números pertencem à referência fornecida, não são metas do Escala.

Este roteiro orienta o agente por AGENTS/templates; não é um serviço, cron, plugin novo ou garantia automática imposta pelo Git. A validação do dossiê é parte da revisão. Nunca compartilhar segredos, dumps ou PII desnecessária durante a entrevista.
