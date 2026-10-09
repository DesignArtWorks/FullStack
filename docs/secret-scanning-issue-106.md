# Secret scanning histórico — #106 / US1

RN-SEC106-001/002. O job `Security / Versioned Secret Scan` mantém o scanner PowerShell de arquivos rastreados e acrescenta Gitleaks v8.24.2, fixado por digest `sha256:b5918eb91b8d2473cec722f066abb4352e4ffdc4ec9f4283ec143aba9ec9ebc4`.

## Cobertura e contrato

O checkout deste job materializa `develop` e o head do PR (inclusive forks via `refs/pull/<n>/head`) sem depth limitado. Em push/manual, o head de entrega é `GITHUB_SHA`. O scan resolve ambos para SHA e percorre a ancestralidade completa dessas duas referências. Branches e tags não alcançáveis por elas estão fora do recorte. Os demais jobs preservam seus checkouts atuais.

`scripts/security/scan-secret-history.sh` rejeita repositório shallow, referência ausente e configuração ausente. Saídas: **0** sem findings não excepcionados; **42** findings; **2** cobertura/execução inválida. Erro nunca vira PASS. Comentários inline `gitleaks:allow` não dispensam o gate.

Os containers recebem o repositório somente para leitura, sem rede, sem capabilities e com `no-new-privileges`. Não autenticam credenciais, não leem `.env` ignorados, não rotacionam valores nem reescrevem Git. Logs têm redaction integral; não publicar Match/Secret ou relatórios brutos.

## Exceções revisadas

A `.gitleaksignore` conserva o controle Slack do harness e o valor `ci-only` de Playwright. A remediação propõe uma terceira ocorrência exata: a chave JWT determinística de 41 bytes do perfil de integração, cujo conteúdo é uma frase dedicada a testes, sem reutilização nos `.env` locais verificados. Sua classificação está sujeita à revisão humana antes de publicação. As exceções locais reais anteriores foram retiradas. Ser desenvolvimento ou teste não justifica ignorar credencial real. Somente placeholder ilustrativo, fixture sem acesso ou falso positivo comprovado pode ser revisado como exceção.

Cada fingerprint inclui SHA, arquivo, regra e linha. Não usar exclusão de diretório/commit nem ampliar padrões para obter CI verde. Nova classificação exige fonte e revisão humana. A remoção das exceções não retira os valores do histórico nem prova revogação.

## Testes e comandos

Na raiz do checkout, com Docker:

```sh
image=zricethezav/gitleaks:v8.24.2@sha256:b5918eb91b8d2473cec722f066abb4352e4ffdc4ec9f4283ec143aba9ec9ebc4
docker run --rm --network none --cap-drop ALL --security-opt no-new-privileges -v "$PWD:/repo:ro" --entrypoint sh "$image" /repo/scripts/security/test-secret-scanner.sh
docker run --rm --network none --cap-drop ALL --security-opt no-new-privileges -v "$PWD:/repo:ro" --entrypoint sh "$image" /repo/scripts/security/test-secret-history.sh
docker run --rm --network none --cap-drop ALL --security-opt no-new-privileges -v "$PWD:/repo:ro" --entrypoint sh "$image" /repo/scripts/security/scan-secret-history.sh /repo refs/remotes/origin/develop HEAD /repo/.gitleaks.toml /repo/.gitleaksignore
```

O último comando requer checkout comum com `.git` no repositório. Para worktree Windows, montar separadamente o Git common directory e definir `GIT_DIR`/`GIT_WORK_TREE`; usar a referência da branch de entrega, pois o HEAD do common directory pertence ao checkout principal.

Os testes montam repositórios temporários só com valores sintéticos. Cobrem GitHub/Slack/AWS/PEM, placeholders, segredo removido, exceção exata, sibling no mesmo commit e reintrodução. O segundo harness prova cada branch de forma independente, exclui branch não selecionada e verifica shallow, referência ausente e falha de execução.

Antes da implementação, o modo `test-secret-history.sh baseline` demonstrou RED: depth=1 retornou 0 onde a detecção histórica exigia 42. O modo normal é o teste permanente; baseline permanece uma reprodução deliberadamente falha do defeito antigo.

## Revisão e limites

Entrevista, H1/H2 e classificação semântica dependem de Wemerson; CI verifica comportamento e cobertura técnica. Ver [plano e ledger](../specs/106-secret-scanner-pilot/tasks.md). US2, rotação e limpeza histórica têm gates próprios. A integração fica bloqueada enquanto existirem findings/erros ou checks obrigatórios pendentes. Scanner verde não prova ausência absoluta de segredo ou invalidez das credenciais.

Rollback: corrigir/reverter a mudança por PR com os controles vigentes, sem remover required checks para contornar falhas.

Referência primária: [Gitleaks v8.24.2 — CLI, configuração e fingerprints](https://github.com/gitleaks/gitleaks/blob/v8.24.2/README.md).

## Remediação preparada em 2026-10-08

Wemerson autorizou tratar as ocorrências e obter CI verde. O [plano operacional](../specs/106-secret-scanner-pilot/history-remediation-plan.md) registra o ensaio: 15 ocorrências removidas e uma fixture sintética proposta como exceção exata; scanner completo e controles negativos PASS. O CMS recebe DATABASE_PASSWORD somente por ambiente, com quatro testes novos RED/GREEN; sete testes de segurança e build Docker/Node 22 PASS. `.env` locais não foram alterados.

Este resultado é local. A publicação ainda exige manutenção excepcional aprovada, pois develop proíbe force push e impõe PR/checks inclusive aos administradores. O CI remoto do PR #124 permanece bloqueado até atualizar as refs e executar os checks no head final. Não afirmar limpeza de main, outras branches, forks, caches ou clones, nem revogação de credenciais.

## Publicação excepcional executada — 2026-10-08

Registro posterior que prevalece sobre estados pendentes anteriores. Wemerson aprovou a exceção somente para este caso e confirmou a identidade no GitHub. Publicação atômica com leases exatos concluída: develop b83673be1c69ee1786f18cc922f1427b37d917db e branch do PR #124 9e098443ab2d1d4c17c3dde2fa740af9af824850. main e demais referências preexistentes não foram alteradas.

A execução manual da base passou, mas o servidor rejeitou a primeira troca: workflow_dispatch não satisfaz checks obrigatórios. Como não há ancestral comum com a develop antiga, um gatilho push restrito à branch temporária security/issue-106-history-maintenance foi acrescentado à base saneada. O run [37832667534](https://github.com/DesignArtWorks/FullStack/actions/runs/37832667534) passou nos oito jobs. A entrega remove esse gatilho temporário; Required Gate e scanner permanecem obrigatórios.

O scanner estrito local passou na ancestralidade completa dos dois SHAs publicados. O run real de PR [37833171774](https://github.com/DesignArtWorks/FullStack/actions/runs/37833171774) passou nos oito jobs, incluindo o scan de ambos os históricos e CI / Required Gate. Wemerson mesclou o PR #124 em 2026-10-08T19:39:11Z, commit 8b7a80a008b990ce6b9848a2d4e73b1d7d6dcf58. Os checks de qualquer entrega posterior exigem execução própria.

As proteções originais foram restauradas e comparadas: PR obrigatório, Required Gate de GitHub Actions, atualização de branch, resolução de conversas e aplicação a administradores; force push e deleções proibidos. Incidente da janela: a restrição ao ator selecionado não persistiu na interface; ao detectar, a regra inteira foi imediatamente restaurada. Não permanece permissão excepcional.

15 ocorrências históricas foram removidas; uma fixture JWT comprovadamente sintética foi aceita por fingerprint exato, somada às duas exceções sintéticas anteriores remapeadas. Nenhuma exclusão ampla de testes foi criada. O CMS deixou de ter fallback de senha PostgreSQL/MySQL e os sete testes de segurança/build passaram no CI real.

T011 está BLOCKED no aceite completo: limpeza das duas refs autorizadas concluída; rotação no marco ainda pendente. T013/T014 também estão BLOCKED no aceite global; registram os fatos de entrega/integração US1 por #124, sem declarar conformidade integral da governança ou conclusão de #106. CON-09 da manutenção é FAIL: suspensão temporária da exigência de PR/force push e registro pré-PR ausente não cumprem a constituição vigente, apesar da autorização humana específica. Proteções restauradas não alteram esse resultado histórico. US2, rotação e fechamento de #106/#99 permanecem pendentes. Histórico em main/outras branches/forks/caches e clones antigos permanece fora do escopo. Reaplicar trabalho antigo somente em clone/base saneada com novo scan; não mesclar o histórico antigo. Credenciais não foram rotacionadas neste desenvolvimento local.
