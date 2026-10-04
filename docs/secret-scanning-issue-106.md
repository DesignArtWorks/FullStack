# Secret scanning historico — issues #99 / #106

O CI usa Gitleaks 8.24.2 fixado por digest, com regras nativas, regras locais,
redacao integral dos valores e `git --log-opts=--all` sobre branches e tags.
O gate de arquivos rastreados em PowerShell continua complementar.

## Excecoes pontuais

O run #87 passou com uma configuracao que excluia dez commits inteiros. Isso
nao demonstrava cobertura dos demais valores presentes nesses commits.
O ajuste remove `allowlist.commits` e o bloco TOML `fingerprints`, que nao e o
mecanismo suportado por essa versao. As vinte ocorrencias ja abrangidas pelas
excecoes anteriores agora constam em `.gitleaksignore`, no formato exato
`commit:arquivo:regra:linha`. Nenhum novo valor foi aceito nesta migracao.

- Duas ocorrencias sao controles sinteticos antigos: fixture Slack e segredo CI.
- As demais sao valores historicos de desenvolvimento, abrangidos pela analise
  da #49. Os dois fingerprints de `093d4ef3` correspondem aos arquivos antes
  de sua reorganizacao em `Backend/cms-strapi`.
- Valores historicos permanecem considerados expostos e nao podem ser
  reutilizados. A excecao nao comprova revogacao/rotacao em servicos externos.
  Essa verificacao pertence ao responsavel pelo ambiente, conforme
  [a decisao da #49](secrets-rotacao-issue-49.md).
- Nao usar fingerprints sem commit, exclusoes de commit/diretorio ou padroes
  genericos para ocultar novos resultados. Mudancas passam por CODEOWNERS.

## Testes e limites

`scripts/security/test-secret-scanner.sh` roda na mesma imagem do CI e verifica
GitHub PAT, Slack, AWS, PEM, placeholder permitido e segredo removido do HEAD.
Tambem prova que uma excecao nao elimina outro segredo na segunda linha do mesmo
arquivo/commit; aceitar as duas ocorrencias exatas passa; reintroduzir o valor
em novo commit ou novo arquivo volta a bloquear. O JSON redigido e conferido por
fingerprint, evitando confundir erro de execucao com deteccao bem-sucedida.

O script rejeita excecoes sem SHA completo e configuracoes locais de exclusao
ampla. `gitleaks git` analisa commits, nao alteracoes locais ainda nao commitadas.
Uma execucao verde significa ausencia de novas deteccoes pelas regras aplicadas,
nao ausencia absoluta de segredos nem validacao de credenciais de producao.

Nao reverter para exclusoes de commits inteiros em caso de falha. Investigar o
resultado redigido, remover/rotacionar valores ativos e registrar qualquer
excecao pontual com justificativa e revisao do mantenedor.

Referencia: [configuracao e fingerprints do Gitleaks 8.24.2](https://github.com/gitleaks/gitleaks/blob/v8.24.2/README.md#additional-configuration).
