# Secret scanning histórico — #106 / US1

RN-SEC106-001/002. O job `Security / Versioned Secret Scan` mantém o scanner PowerShell de arquivos rastreados e acrescenta Gitleaks v8.24.2, fixado por digest `sha256:b5918eb91b8d2473cec722f066abb4352e4ffdc4ec9f4283ec143aba9ec9ebc4`.

## Cobertura e contrato

O checkout deste job materializa `develop` e o head do PR (inclusive forks via `refs/pull/<n>/head`) sem depth limitado. Em push/manual, o head de entrega é `GITHUB_SHA`. O scan resolve ambos para SHA e percorre a ancestralidade completa dessas duas referências. Branches e tags não alcançáveis por elas estão fora do recorte. Os demais jobs preservam seus checkouts atuais.

`scripts/security/scan-secret-history.sh` rejeita repositório shallow, referência ausente e configuração ausente. Saídas: **0** sem findings não excepcionados; **42** findings; **2** cobertura/execução inválida. Erro nunca vira PASS. Comentários inline `gitleaks:allow` não dispensam o gate.

Os containers recebem o repositório somente para leitura, sem rede, sem capabilities e com `no-new-privileges`. Não autenticam credenciais, não leem `.env` ignorados, não rotacionam valores nem reescrevem Git. Logs têm redaction integral; não publicar Match/Secret ou relatórios brutos.

## Exceções revisadas

A `.gitleaksignore` conserva apenas duas ocorrências sintéticas antigas: o controle Slack do harness e o valor `ci-only` de Playwright. As 18 exceções históricas anteriores para valores locais foram retiradas conforme a entrevista. Ser desenvolvimento ou teste não justifica ignorar credencial real. Somente placeholder ilustrativo, fixture sem acesso ou falso positivo comprovado pode ser revisado como exceção.

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
