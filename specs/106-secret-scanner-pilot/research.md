# Pesquisa e evidências — #106 US1

Data: 2026-10-08. H1 US1 aprovado por Wemerson nesta conversa com “sim”. O recorte permite scripts/testes/CI; H2 continua pendente.

Fonte primária: https://github.com/gitleaks/gitleaks/blob/v8.24.2/README.md. CLI suporta `--exit-code`, `--log-opts`, `--redact`, `--gitleaks-ignore-path` e `--ignore-gitleaks-allow`. Imagem/digest reutilizados do controle preparado em `security/issue-99-production-preflight`, sem adicionar dependência runtime; `docker run ... version` retornou v8.24.2.

Inventário de exceções: 20 fingerprints anteriores; dois sintéticos preservados, 18 valores históricos locais retirados da allowlist. Os valores não foram publicados nem usados para login. Classificação de validade não é função deste scanner; uma ocorrência suspeita continua bloqueando até revisão/remediação.

RED observado antes do wrapper: scan sobre clone sintético depth=1 retornou 0 em vez de 42 para segredo removido. Harness encerrou com 1 e mensagem RED. GREEN posterior: detecção dos padrões, exceção exata e reintrodução; histórico completo, refs selecionadas, shallow/ref ausente e erro do scanner. Cada branch é também testada contra base limpa para evitar mascaramento por finding na outra branch.

Tooling retorna 0/42/2. Fetch completo de refs explícitas; SHA desenvolvido e head PR registrados no log. PR head via namespace do repositório base, sem usar URL de fork não confiável. Containers sem rede; nenhum endpoint de serviço é chamado. Timeout do job ampliado de 5 para 15 minutos; sem SLA de aplicação criado.

Histórico real: resultado será registrado no ledger após execução; não inferir limpeza a partir dos testes sintéticos. CI completo deve executar no PR e permanece autoridade para integração.

### Evidência local final

Histórico completo das duas refs na base 0efcc18: **16 findings**, BLOCKED (status interno 42). Nenhum valor foi impresso. Os 18 fingerprints removidos não equivalem à contagem final: cobertura/ref e deduplicação do scanner determinam as ocorrências detectadas. Harness final após relatório temporário sanitizado: PASS. Links Markdown e scanner PowerShell: PASS. H2 PENDING; CI remoto pendente.
