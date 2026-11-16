# Dossiê de retomada — #99 SCA e gates

Data: 2026-11-16. Responsável: Wemerson. Fonte: conversa atual; roteiro em `docs/ai/entrevista-socratica.md`. Registro de autorização concreta, sem simular novas rodadas de entrevista.

## 1. As três canônicas

- O que: revalidar SCA, verificar patches de braces/zod e atualizar quando houver correção compatível; depois concluir #106/#109 e gate #99.
- Para quem: mantenedor do Escala, com entrega rastreável em develop.
- Sucesso: dependências sem High/Critical não aceitos, gates reais aprovados e comunicação fiel no Slack. Fechamento depende dos critérios das issues.

## 2. Proveniência

| ID | Classe | Fonte e alcance |
| --- | --- | --- |
| P01 | DECISÃO | Wemerson, mensagem desta conversa: “Revalidar SCA e corrigir dependências ainda bloqueantes”, atualizar braces/zod se houver correção; autorização de investigação/patch compatível e validação. |
| P02 | DECISÃO | Mesma mensagem: concluir #106/#109, rodar gate #99 e atualizar Slack nos canais corretos. Não resolve por si decisões abertas de US2 nem autoriza bypass/main. |
| P03 | EVIDÊNCIA | Registry npm consultado em 16/11: braces 3.0.3, zod 4.6.7, koa-session 7.0.2 com zod ^3.24.1; última linha 3: 3.25.76. |
| P04 | EVIDÊNCIA | Advisory GitHub braces retirado em 23/10; Zod apresenta divergência GitHub/NVD. SCA sem exceções determinará resultado técnico, sem presumir PASS. |
| P05 | RESTRIÇÃO | Constituição CON-01–09; fases/revisão humana; #106 AB-001/003 e #109 exigem decisões/evidências próprias. |

## 3. Termos quantificados

“Atualizar pacote corrigido” significa versão publicada e compatível com os consumidores, sem forçar Zod 4 no koa-session dependente de API 3. “Gate final” inclui testes negativos, SCA, secrets, lint/typecheck/build, Docker/health/OpenAPI, CI e Codex Security.

## 4. Riscos e alternativas

Atualizar major à força pode quebrar sessões do CMS. Advisory retirado não justifica upgrade artificial; exceção vencida não pode ser prorrogada automaticamente. Scan sem supressões verifica se ainda existe finding. Atualização documental preserva fatos históricos e registra novo estado separado.

## 5. Abertas

- Wemerson: AB-001 da #106 perguntada nesta conversa; resposta pendente. AB-003 exige contrato local aprovado antes de autenticar credenciais reais.
- Wemerson: ambiente alvo e ponto externo de verificação da #109 ainda não identificados.
- H2: revisar resultado/diff desta fatia antes de integração; nenhum silêncio equivale a aceite.
- Não há decisão de negócio pendente para consulta de registry/SCA e patch compatível autorizado em P01.
