# Retomada SCA da #99

Data: 2026-10-27; responsável: Wemerson. Este recorte preserva o PR129/#103 em branch separada.

## 1. Três canônicas

O que: corrigir as dependências High/Critical do gate SCA sem quebrar consumidores nem ocultar achados. Para quem: mantenedor do Escala e usuários CMS/editorial. Sucesso: patches compatíveis, testes/build reais e SCA demonstrado; remanescentes explícitos impedem merge.

## 2. Proveniência

| ID | Classe | Fonte/alcance |
| --- | --- | --- |
| PROV-SCA001 | EVIDÊNCIA | Relatório fornecido pelo usuário e CI127/run40533959727/job122294024613: CMS12 ocorrências, 11High/1Critical; não comprova exploração em produção |
| PROV-SCA002 | DECISÃO | Wemerson nesta conversa: “Aprovar correção e validação do SCA”, em resposta ao H1 de atualização compatível dos sete pacotes, testes e investigação Zod3 sem troca incompatível/exceção automática |
| PROV-SCA003 | RESTRIÇÃO | AGENTS/CON-08/09: preservar gates; H2 após MVP, integração apenas via PR/checks; produção/main e #99 continuam bloqueadas |
| PROV-SCA004 | EVIDÊNCIA | npm oficial: último Zod3=3.25.76; koa-session7.0.2 exige3 e usa function().args().returns(); Strapi5.59 ainda fixa versões vulneráveis; upgrade geral não resolve sozinho |

Dossiê de retomada baseado na decisão humana técnica explícita e nos requisitos AppSec fornecidos. Não simula entrevista nova em cinco fases/seis categorias. H1 autorizado; H2 desta fatia ainda pendente. Certificados C:/certs somente em downloads locais temporários, TLS verificado; não versionar CA.

## 3. Termos observáveis

“Corrigir SCA” = scanner reconhecido com base atual e escopo completo, sem novo ignore automático. “Compatível” = preservar majors onde possível e testar consumidores; Zod4 não substitui silenciosamente API3. jsdom é ferramenta de teste DOM, sem integração/runtime novo.

## 4. Riscos e alternativas

Override Zod4 global quebra koa-session. Importar zod/v3 a partir de pacote4 mantém código vulnerável e apenas oculta versão. Atualizar Strapi inteiro não elimina dependências atuais afetadas e amplia escopo. Regras de arrays sem limite continuam precisando limite no consumidor mesmo com Zod4 corrigido.

## 5. Abertas

Zod3 em koa-body/session: dono Wemerson/segurança; falta decidir disposição formal após evidência de alcançabilidade. Nenhum aceite autorizado. Novos achados no frontend devem ser explicitados e corrigidos no recorte aprovado antes de declarar gate verde. H2/merge pendentes.
