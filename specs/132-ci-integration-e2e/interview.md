# Dossiê — correção do CI da PR #132

Data: 2026-10-30. Interlocutor: Wemerson. Escopo: correção limitada dos dois jobs da PR #132; sem merge. Registro de fontes e confirmação concreta reutilizada desta conversa, sem simular entrevista completa.

## 1. As três canônicas

- O que: corrigir a leitura de relatórios de testes aninhados e autenticar o usuário B com seu tenant explícito no teste E2E.
- Para quem: mantenedores revisando a PR #132 para develop.
- Sucesso: verificador aceita testes executados em XML e rejeita ausências/falhas/skips; E2E comprova sessão B, dados B e negação de acesso a A; novos checks remotos devem passar.

Enquadramento/exploração: pedido humano de diagnóstico dos jobs. Aprofundamento: logs do run 41065747746 e relatórios locais Surefire. Ampliação: aceitar qualquer TXT vazio enfraqueceria o gate; retirar a verificação cross-tenant esconderia o problema. Síntese: proposta concreta apresentada pelo agente e confirmada por Wemerson: “Vamos corrigir para que consigamos publicar a PR”. Não houve cinco rodadas de entrevista nem revisão humana pós-MVP nesta sessão; não são alegadas.

## 2. Proveniência

| ID | Classe | Fonte e alcance |
| --- | --- | --- |
| P01 | EVIDÊNCIA | Run 41065747746: Maven 206 testes, zero falhas/erros/skips; gate TXT rejeita AuthHttpIntegrationTest. XML local agrega 10 testes; TXT externo registra zero. |
| P02 | EVIDÊNCIA | Job 124096561016: login de Bob permanece em /en/login. Playwright define tenant-a; mock exige que companySlug corresponda ao usuário. |
| P03 | DECISÃO | Wemerson, nesta conversa: “Vamos corrigir para que consigamos publicar a PR”, em resposta à proposta limitada a workflow/testes. H1 aprovado para esse recorte e publicação na PR #132. |
| P04 | RESTRIÇÃO | AGENTS/constituição: manter isolamento, checks e revisão; não integrar por inferência. |

Pergunta real apresentada: “Autoriza aplicar essa correção limitada ao workflow e aos testes?”; resposta P03. Categorias esclarecimento/evidência/pressupostos/implicações/alternativas/meta foram examinadas na investigação e proposta, sem atribuir respostas fictícias ao usuário.

## 3. Termos quantificados

“Corrigir” = dois defeitos descritos em P01/P02; nenhum novo comportamento de produção, contrato, regra de negócio ou dependência runtime.

## 4. Riscos e alternativas

O gate deve falhar para XML ausente, inválido, vazio, inconsistente, com falhas/erros/skips; controles negativos necessários. Autenticação B usa o callback NextAuth real já empregado pela suíte de avatar, sem forjar cookie nem relaxar o mock. Fluxo visual A permanece coberto. Relatórios locais anteriores não comprovam nova execução do backend.

## 5. Abertas e limites

Sem decisão de negócio bloqueadora neste recorte aprovado. H2/revisão pós-correção permanece com Wemerson antes de merge. Rastreabilidade global da refatoração original e validação runtime integral não são concluídas por esta correção. Gate do recorte: autorização explícita P03; sem alegação de entrevista global VALIDADA.
