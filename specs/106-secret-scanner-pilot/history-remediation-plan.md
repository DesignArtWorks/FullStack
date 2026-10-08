# Plano operacional de limpeza — #106 / PR #124

Estado: ensaio isolado preparado em 2026-10-08; publicação e CI remoto pendentes.
Autorização de preparação: Wemerson, “Então vamos tratar as ocorrências e o obter o CI verde”. Este pedido libera a correção e o ensaio; não altera por si só a proteção de branches.

## Escopo e proveniência

RN-SEC106-004; FR-005/007; T008/T011. O run 37818305235 detectou 16 ocorrências na ancestralidade completa de develop e da branch do PR. Seis outros jobs passaram; Required Gate falhou por depender do scanner. O objetivo é eliminar as ocorrências sem enfraquecer o scanner.

| Referência | SHA remoto esperado antes da manutenção |
| --- | --- |
| refs/heads/develop | 0efcc18f0dc3a8cc53974de363d3f6128ea73cac |
| refs/heads/security/issue-106-secret-scanner-pilot | 36a13cc977568e97c54b29547e9a0c5c740ee48c |

Validar novamente ambos os SHAs imediatamente antes de qualquer atualização. Se mudarem, interromper, incorporar o trabalho novo em outra cópia e repetir as provas. Não usar push --mirror, --all, --force genérico ou atualizar main/tags/outras branches.

## Tratamento das ocorrências

| Origem histórica | Ocorrências | Tratamento |
| --- | --- | --- |
| Backend/cms-strapi/.env.local | 6 | Retirar o arquivo da história selecionada |
| Backend/java-app1/demo/.env | 1 | Retirar o arquivo da história selecionada |
| docker-compose.yml | 2 | Substituição literal privada dos valores históricos |
| Backend/cms-strapi/Docker/docker-compose.yml | 1 | Substituição literal privada |
| Backend/cms-strapi/config/database.ts | 1 | Substituição histórica; remover fallback de senha no código atual |
| Data/postgres/init.sql e Dados/postgres/init.sql | 4 | Substituição literal privada |
| Backend/java-app1/demo/src/test/resources/application-test.yml | 1 | Proposta de exceção exata para fixture comprovadamente sintética |

São 15 ocorrências removidas e uma classificação sintética proposta. A fixture é Base64 de uma frase explicitamente dedicada a testes, com 41 bytes decodificados; aparece no perfil test e os testes de integração usam PostgreSQL/Redis efêmeros por Testcontainers. Não é evidência de autenticação em um serviço externo. A aplicação implantada exige JWT_SECRET via ambiente. Não excluir todo src/test: cópias futuras da fixture e credenciais reais em testes continuam sujeitas ao scanner.

O ensaio conserva as duas exceções sintéticas já aprovadas (Slack do harness e Playwright) e remapeia seus SHAs, pois a reescrita altera commits descendentes. A nova exceção exige revisão humana antes de publicação. Nenhum valor de credencial aparece neste documento, no commit ou no relatório público.

Verificação adicional sem imprimir valores: a fixture não é reutilizada nos .env locais existentes da raiz, backend e frontend; em ambas as árvores originais, a busca literal só encontra application-test.yml. Essa verificação não equivale a testar validade em serviços externos.

## Procedimento ensaiado

1. Inventariar findings usando a imagem Gitleaks v8.24.2 com digest fixado no workflow. Relatório público redigido; dados privados somente em diretório temporário gerenciado.
2. Criar clone bare independente com --no-local, sem alterar o Git compartilhado, seus 14 worktrees, stash, mudanças de UI ou arquivos .env ignorados.
3. Usar git-filter-repo v2.47.0, --sensitive-data-removal, --no-fetch e --refs com exatamente as duas referências acima. Remover somente os dois arquivos sensíveis; substituir literalmente oito valores distintos das 15 ocorrências por replace-with-local-secret. A fixture de testes permanece funcional.
4. Comparar árvores: develop muda somente o fallback histórico em config/database.ts; o restante da árvore atual é preservado. Na entrega, remover fallbacks PostgreSQL/MySQL de DATABASE_PASSWORD; manter leitura por env. Acrescentar testes de ausência/presença da variável, sem valores reais.
5. Remapear fingerprints específicos pelo commit-map e verificar ancestralidade entre develop e entrega. Rodar ambos os harnesses e o wrapper do histórico completo. Exigir status 0, sem findings não excepcionados; status 42 ou 2 interrompe a operação.
6. Registrar comparação de árvores, resultados, SHAs finais e inventário de refs em evidência sanitizada. Revisão independente assistiva não substitui aprovação humana.

O Git do Windows encontrou um caminho antigo com espaço final e limites de tamanho. No clone temporário bare, core.protectNTFS=false permite importar o objeto antigo sem renomeá-lo; no checkout temporário core.longpaths=true permite os caminhos atuais. Não modificar configurações globais nem caminhos do produto por causa do ensaio.

## Proteção e publicação: parada humana obrigatória

Configuração de develop lida no GitHub em 2026-10-08: PR obrigatório, CI / Required Gate obrigatório (GitHub Actions), branch atualizada, resolução de conversas e proteção aplicada a administradores; force push e deleções proibidos. Um PR normal mantém os commits antigos na ancestralidade e não resolve esta limpeza.

O AGENTS.md determina que push direto/bypass não contorne governança. Portanto não publicar o rewrite nem alterar proteções sob a aprovação genérica de implementação. O mantenedor deve aprovar uma janela excepcional de manutenção do histórico, o ator executor, o ajuste mínimo de proteção estritamente necessário e sua restauração. Não retirar ou falsificar checks para aceitar este PR. Registrar os estados antes/depois e restaurar a proteção original antes de qualquer merge de produto.

Proposta concreta para essa autorização: publicar primeiro a base sanitizada em branch temporária de manutenção e executar workflow_dispatch para obter CI / Required Gate aprovado no SHA dessa base; a prova adicional do scanner completo usa a política estrita da entrega. Só após ambas passarem, permitir force push para o ator mantenedor e suspender exclusivamente a exigência de PR pelo tempo da troca de refs. Manter Required Gate, proteção para administradores, resolução de conversas e demais regras. Se o servidor exigir outro relaxamento ou qualquer check falhar, interromper e voltar à revisão humana. A manutenção é uma exceção explícita ao fluxo ordinário de PR, não um merge com checks vermelhos.

Na janela aprovada: suspender pushes/merges, conferir os dois SHAs esperados, atualizar as duas referências consistentemente usando --atomic e leases explícitos por referência (se o servidor não suportar atomic, interromper e revisar o procedimento). Restaurar a proteção; buscar as refs do GitHub em clone novo; executar scanner completo com a política da entrega e rerodar o workflow real do PR. Só considerar CI verde quando todos os jobs obrigatórios do head remoto passarem. O PR continua draft até revisão H2/H3 e checks reais aprovados.

Ensaio validado: develop sanitizada 43a8f12e4774eccfe28ef61623485d4ab0fa90ff; commit de código testado c4c1f3f3080ff4e95fb90adb97acc0205180f37c. Ambos os harnesses e o scanner completo retornaram 0. Os sete testes CMS e o build em Docker/Node 22 passaram. Documentação posterior pode mudar somente o head de entrega; repetir scanner/diff nesse head final e registrar o SHA no ledger externo, sem autorreferência no commit.

## Reintrodução, caches e recuperação

Os commits contaminados também são alcançáveis por main e diversas branches fora do escopo. O ensaio não limpa essas refs, tags, forks, refs internas de PR, caches nem clones existentes. GitHub Support pode ser necessário para refs/caches inacessíveis. O dono deve decidir limpeza/aposentadoria das referências restantes antes de integrá-las nas branches limpas; não fazer merge do histórico antigo.

Colaboradores devem preferir clone novo; trabalho não publicado deve ser reaplicado cuidadosamente, sem merges do histórico antigo e com scanner. Preservar o trabalho local de UI e .env; não resetar o checkout compartilhado nem executar GC nele. Dados privados temporários não devem ser publicados como backup remoto.

Recovery: manter o mapa de commits e os SHAs anteriores em evidência privada; em caso de erro, congelar a integração e corrigir o histórico sanitizado. Restaurar a proteção é obrigatório mesmo quando a publicação falha. Não repor segredos no remoto como rollback automático. Nova operação sobre histórico requer revisão do mantenedor.

As credenciais locais não foram rotacionadas nem autenticadas neste recorte. A aceitação temporária de desenvolvimento já registrada permanece até o marco combinado após #103/#106/#109 e antes do gate final #99. Scanner limpo não revoga credenciais copiadas nem prova ausência absoluta de segredos.

Fontes: [GitHub: removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository), [git-filter-repo v2.47.0](https://github.com/newren/git-filter-repo/tree/v2.47.0).
