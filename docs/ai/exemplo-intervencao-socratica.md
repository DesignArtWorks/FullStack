# Exemplo de aplicação de IA — intervenção socrática

Material didático adaptado da referência fornecida por Wemerson em 2026-10-08. Vetor é uma plataforma fictícia; não são requisitos, entrevistas reais ou evidências do Escala. O recorte abaixo não demonstra todas as fases/categorias nem uma entrevista validada completa.

## Pedido inicial e linha de base

O gerente pede painel de descontos para o comercial, exportação Excel, pizza do mês, uso diário, interface intuitiva/moderna, carregamento rápido, todos os descontos, entrega até sexta e 100% de cobertura.

Antes de entrevistar, registrar as ambiguidades percebidas: adjetivos, universal "todos", prazo sem escopo e cobertura sem relação com riscos. Essa linha de base é observação do analista; não é descoberta com stakeholder.

## Recorte de diálogo

**Exploração / esclarecimento**, após "intuitivo":

> Qual tarefa um gerente precisa completar sem ajuda na primeira visita ao painel?

Resposta ilustrativa da referência: encontrar desconto total do mês em menos de dez segundos.

**Aprofundamento / evidências**, após "uso diário":

> Qual registro sustenta a afirmação de que os gerentes usam esse número todos os dias?

Resposta ilustrativa: não foi medido; a equipe comenta. Registrar [PRESSUPOSTO], sem transformar a opinião em fato.

**Ampliação / perspectiva alternativa**:

> Como o time de dados descreveria o problema que resolve hoje com a extração manual?

Resposta ilustrativa: extração leva quarenta minutos por mês. O intervalo mensal conflita com a alegação diária e revela que a urgência pode referir-se à extração, não ao carregamento do painel.

Uma síntese após quatro respostas reais teria o formato "Confirmado: ...; suposto: ...; conflitos: ...", seguida de uma pergunta. Este recorte contém três respostas ilustrativas e não simula uma quarta para preencher a contagem.

## Dossiê didático — cinco seções

Estado: INCOMPLETO. Fontes: texto e demonstração fornecidos; sem medições/entrevista reais nesta execução.

### 1. As três canônicas

- **O quê:** visualizar o desconto total mensal; fronteira de quais descontos entram ainda não definida.
- **Para quem:** gerentes comerciais, conforme pedido fictício.
- **Sucesso testável:** proposta da referência é um usuário de primeira visita encontrar o total sem ajuda em menos de dez segundos; exige confirmação e protocolo antes de virar aceite de produto.

### 2. Registro de proveniência

| ID | Afirmação | Classe | Origem / limite |
| --- | --- | --- | --- |
| EX-001 | Os gerentes usariam o número diariamente | [PRESSUPOSTO] | Fala fictícia sem medição |
| EX-002 | Encontrar o total em menos de dez segundos | [DECISÃO] | Resposta ilustrativa; não meta do Escala |
| EX-003 | Extração manual leva quarenta minutos por mês | [EVIDÊNCIA] | Relato do exemplo, ainda sem cronômetro/log independente |
| EX-004 | Exportação Excel é necessária | [ABERTA] | Pedido não identifica consumidor/uso posterior |
| EX-005 | Uso diário versus extração mensal | [CONFLITO] | Contextos podem ser distintos; não resolver sozinho |

### 3. Termos quantificados — antes e depois

| Antes | Depois / estado |
| --- | --- |
| Intuitivo | Encontrar total sem ajuda em menos de dez segundos no exemplo |
| Rápido | ABERTA: separar extração manual de latência do painel |
| Moderno | ABERTA: comportamento/critério visual não definido |
| Todo desconto | ABERTA: período, status, cancelamentos, arredondamento e permissões |
| Testes completos / 100% | ABERTA: escopo, riscos e demonstração necessária |

### 4. Riscos examinados e perspectivas

Cobertura de 100% sem escopo não comprova regra correta; prazo sem recorte pode empurrar decisões implícitas ao código. A perspectiva de dados questiona o uso diário e a natureza da demora. Escolha de gráfico/exportação sem finalidade pode criar um resultado que não resolve a necessidade.

### 5. Perguntas abertas com dono

| Pergunta | Dono no exemplo | Recorte bloqueado |
| --- | --- | --- |
| Quem consome o Excel exportado? | Gerente comercial | Exportação |
| Que descontos compõem o total? | Gerente comercial | Cálculo/relatório |
| Qual latência é aceitável para a consulta? | Gerente comercial | Critério de performance |
| Que escopo cabe no prazo? | Gerente comercial | Prazo e aceite |
| O que deve ser demonstrado pelos testes? | Gerente comercial e responsável de QA a designar | Estratégia de testes |

O dossiê explicita o que não pode ser deduzido só reescrevendo o pedido. Ele não libera implementação com essas decisões pendentes.
