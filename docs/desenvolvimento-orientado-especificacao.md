# Desenvolvimento orientado por especificação no Escala

## Ponto de entrada

Ler [constituição](../.specify/memory/constitution.md), `AGENTS.md` e docs/ADRs do contexto. Primeira aplicação: [spec](../specs/120-governanca-especificacao/spec.md), [análise](../specs/120-governanca-especificacao/analysis.md), [plano](../specs/120-governanca-especificacao/plan.md) e [tarefas](../specs/120-governanca-especificacao/tasks.md).

O processo adapta o [Spec Kit oficial](https://github.com/github/spec-kit). Constituição é do projeto; spec/plan/tasks são de cada mudança. Esta entrega fornece documentos/templates locais, não instala CLI, scripts ou comandos speckit. Não executar init que sobrescreva instruções existentes sem plano de integração.

## Estrutura versionada

```text
.specify/
  memory/constitution.md
  templates/spec-template.md
  templates/plan-template.md
  templates/tasks-template.md
specs/
  <numero>-<slug>/
    spec.md
    plan.md
    tasks.md
    analysis.md              # quando necessário
    contracts/              # quando houver contrato relevante
```

Usar número da issue. Se uma CLI futura precisar de outra convenção, mapear pasta ↔ issue explicitamente e manter branch `<tipo>/issue-<numero>-<slug>`.

## Fluxo prático

1. Identificar issue, atualizar origin e isolar branch do develop mais recente. Preservar mudanças locais; worktree é opção se o checkout estiver ocupado.
2. Copiar [spec-template](../.specify/templates/spec-template.md). Definir o quê/por quê, regras de origem, histórias, limites e critérios. Resolver decisões de negócio antes do trecho dependente.
3. Copiar [plan-template](../.specify/templates/plan-template.md). Analisar código/testes, declarar arquivos, desenhar arquitetura, contratos/dados/UX e Constitution Check CON-01 a CON-09.
4. Copiar [tasks-template](../.specify/templates/tasks-template.md). Decompor tarefas com FR/RN, dependências, arquivos e aceite; preparar registro de verificações e commits.
5. Implementar na ordem das dependências. Marcar checkbox após evidência; descoberta que muda premissa retorna a spec/plan.
6. Validar gates, diff, docs e Constitution Check final. Registrar falha/não executado/N/A com motivo.
7. Commit/push e PR develop. Após checks/aprovação/merge, revisar promoção main e fechar issue somente ao completar a entrega oficial.

## Identificadores e commits

Regra existente conserva ID. Regra sem ID pode receber `RN-PLANNING-001`, com fonte exata e responsável; ID não cria regra. FR/US/SC/T são locais à spec: usar `120-governanca-especificacao:T003` fora dela.

Exemplo ilustrativo de futura regra de escala, não ID atribuído ao código atual:

```text
RN-PLANNING-001 → FR-003 / US1 → T004 → teste RN-PLANNING-001
              → comentário no ponto da regra → commit real → PR real
```

Java pode usar `@DisplayName("RN-PLANNING-001: ...")` ou comentário no teste. Código referencia o ponto da invariante, não todas as linhas. Documento vincula seção/artefato e revisão, sem teste de runtime artificial.

Busca a partir da raiz:

```powershell
rg -n --glob '!pnpm-lock.yaml' 'RN-PLANNING-001' specs Backend/java-app1/demo/src Frontend/web-app3/escala/src
git log --all --oneline --grep='120-governanca-especificacao'
```

Exemplo de mensagem desta adoção:

```text
docs: consolida constituicao do Escala (#120)

Spec: 120-governanca-especificacao
Tasks: T002 T003 T004
Refs #120
Validation: registrar comandos/resultados realmente executados
```

Ledger registra SHA real em commit posterior ou PR; após squash, registrar SHA de merge e preservar IDs. Não usar fechamento automático antes da promoção exigida.

## Conteúdo do PR e limites

Problema/resultado, issue/spec, tarefas, arquivos/contratos afetados, Constitution Check, comandos/resultados, checks do head, riscos/pendências e rollback. GitHub/CI é autoridade para integração. Documento também aciona todos os jobs do workflow sem filtros por caminho.

Constituição e templates são controles documentais. Não há novo hook ou gate automático de rastreabilidade nesta versão. Automação futura exige issue e validação própria; checklist não impõe automaticamente regras no Git.

## Mudança de princípio

Implementação comum não modifica a constituição. Propor emenda separada com riscos e aprovação dos responsáveis; atualizar versão/histórico, ADRs, templates e specs afetadas. Justificativa unilateral não é exceção aprovada.
