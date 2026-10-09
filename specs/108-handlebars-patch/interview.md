# Dossiê de retomada — correção transitiva do CMS #108

Registro consolidado em 2026-10-09. Roteiro: [entrevista socrática](../../docs/ai/entrevista-socratica.md). Contexto reutilizado: [entrevista #106](../106-secret-scanner-pilot/interview.md). Responsável: Wemerson.

Este documento registra respostas/autorização reais já existentes na conversa e a retomada do recorte técnico. Não afirma que este arquivo existia antes do patch nem que houve uma nova entrevista completa em cinco fases. A aprovação H1 precedeu o manifest; o dossiê versionado foi acrescentado após a revisão de #127 apontar sua ausência. Essa lacuna documental fica explícita; H2 continua pendente e não há autorização inferida de merge.

## 1. As três canônicas

| Canônica | Resposta do recorte e origem |
| --- | --- |
| O que construir | Correção limitada da dependência transitiva vulnerável do CMS, preservando funcionamento; escopo da pergunta H1 aprovada abaixo |
| Para quem | Mantenedor do Escala/Wemerson, responsável por validar CMS e CI; contexto da solicitação de sanar ocorrências e obter CI verde |
| Sucesso testável | Testes e build do CMS passando e todos os checks obrigatórios verdes, sem ignorar CVEs ou alterar proteções; pergunta H1 e pedido de CI verde |

### Histórico humano disponível e reutilização

Q-HB001 (H1, pergunta real enviada antes do patch): “Você aprova o H1 deste novo bloqueio: atualizar apenas a dependência transitiva handlebars do CMS de 4.7.9 para 4.7.10, ajustar package.json/package-lock.json e testes/documentação necessários, validar o CMS e todos os checks, sem ignorar as CVEs nem alterar proteções?”

Resposta humana literal em 2026-10-08: **“Aprovar correção e validação”**. Fonte: resposta do usuário ao request_user_input_async, call_c2ba01aa2e8441778698d049e6a126f3, item 0, nesta conversa. Trata-se da aprovação concreta de proposta técnica/H1; não a reclassificar como diálogo socrático completo.

Decisões reutilizadas: fluxo por fases com revisão antes de código existente e após MVP (resposta humana na mesma conversa); pedido de tratar ocorrências e obter CI verde. Nova restrição humana em 2026-10-08: “não use CA corporativo para incluir no código do docker, pois ele é restrito a esta maquina ele é apenas para baixar arquivos para ter acesso a internet”.

Fases/categorias da entrevista #106 permanecem no dossiê de origem. No novo recorte, evidências e alternativas foram examinadas tecnicamente pelo agente e investigador, não apresentadas como respostas humanas adicionais. Não há adjetivo de negócio, direito, cálculo, tenant ou ator operacional novo a decidir. Apresentar este dossiê no H2 permite revisar a documentação produzida sem pedir novamente a autorização H1 já dada.

## 2. Registro de proveniência

| ID | Classe | Afirmação / fonte / alcance |
| --- | --- | --- |
| PROV-HB001 | DECISÃO | Wemerson aprovou Q-HB001; escopo exato de patch/validação, não merge ou nova história |
| PROV-HB002 | EVIDÊNCIA | CI #126 run 37836456753 detectou dois CVEs Critical em 4.7.9; não comprova exploração no CMS |
| PROV-HB003 | EVIDÊNCIA | Avisos oficiais GHSA-p8wg-vrv2-v86f e GHSA-8r5x-fm3f-whwj identificam 4.7.10 corrigida; fontes em spec/plan |
| PROV-HB004 | RESTRIÇÃO | CA corporativa somente para download nesta máquina, conforme fala acima; nenhum certificado/Dockerfile/Compose versionado |
| PROV-HB005 | EVIDÊNCIA | RED em 4.7.9, GREEN de 13 testes e build em Node22 e SCA local; limites/comandos no plan |
| PROV-HB006 | ABERTA | Acessibilidade dos cenários a atacante no CMS implantado não demonstrada; dono: segurança/mantenedor; não necessária para cumprir política SCA |
| PROV-HB007 | CONFLITO | Ausência inicial dos artefatos obrigatórios; corrigida documentalmente após revisão, sem alegar registro prévio nem emenda; H2 pendente |

## 3. Termos quantificados — antes e depois

| Expressão | Fronteira observável |
| --- | --- |
| “CI verde / todos os checks” | Oito jobs do Monorepo CI no head efetivo, todos success; job cancelado/skipped não satisfaz |
| “somente correção” | Override 4.7.10, metadados correspondentes do lock, regressões e artefatos necessários; sem mudança de Strapi, contratos ou proteção |
| “validar CMS” | Instalação determinística, resolução dos dois consumidores, testes, geração real e build; não alegar teste de cada tela ou ataque em produção |

## 4. Riscos examinados e perspectivas alternativas

Compatibilidade de geração: verificar strings/helpers/partials/loops, AST válida e geração Strapi/node-plop. Atualizar Strapi inteiro ampliaria o recorte; remover geradores quebraria função existente; ignorar CVEs contrariaria H1. Essas comparações são análise técnica, não novas decisões humanas. Release 4.7.10 muda iteração Map/Set/generators; não afirmar cobertura universal. A aprovação ex post de documento não comprova que foi escrito antes. Rollback do patch reintroduz CVEs.

## 5. Abertas com dono

| Questão / pendência | Dono | Efeito |
| --- | --- | --- |
| Revisar MVP, dossiê/diff e aprovar H2 | Wemerson | Merge/fase posterior BLOCKED até decisão; PR de revisão permitido |
| Alcance de exploração em implantação real | Segurança/mantenedor | Sem claim de exploração ou segurança universal; patch SCA não depende disso |
| Rotação/US2/CON-09 da manutenção histórica #106 | Wemerson | Fora deste patch; este dossiê não resolve nem autoriza esses recortes |
