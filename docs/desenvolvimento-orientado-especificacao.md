# Desenvolvimento orientado por especificação no Escala

## Ponto de entrada

Ler [constituição](../.specify/memory/constitution.md), `AGENTS.md` e docs/ADRs do contexto. Primeira aplicação: [spec](../specs/120-governanca-especificacao/spec.md), [análise](../specs/120-governanca-especificacao/analysis.md), [plano](../specs/120-governanca-especificacao/plan.md) e [tarefas](../specs/120-governanca-especificacao/tasks.md).

Antes de qualquer implementação, executar [entrevista socrática](ai/entrevista-socratica.md) e registrar [interview.md](../.specify/templates/interview-template.md) na pasta da mudança. Na retomada, conferir escopo e respostas válidas; nunca inventar diálogo retrospectivo. O modo padrão aprovado é por fases, com H1 antes de alterar código existente, H2 após MVP e H3 para decisão fora da spec.

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
    interview.md             # dossiê e gate pré-implementação
    analysis.md              # quando necessário
    contracts/              # quando houver contrato relevante
```

Usar número da issue. Se uma CLI futura precisar de outra convenção, mapear pasta ↔ issue explicitamente e manter branch `<tipo>/issue-<numero>-<slug>`.

## Fluxo prático

1. Identificar issue, atualizar origin e isolar branch do develop mais recente. Preservar mudanças locais; worktree é opção se o checkout estiver ocupado.
   Antes de implementar, entrevistar/validar o dossiê, apresentar proposta de entendimento fora do papel de entrevistador e registrar sua validação humana. Templates podem ser preparados como rascunho sem liberar código dependente.
2. Copiar [spec-template](../.specify/templates/spec-template.md). Definir o quê/por quê, regras de origem, histórias, limites e critérios. Resolver decisões de negócio antes do trecho dependente.
3. Copiar [plan-template](../.specify/templates/plan-template.md). Analisar código/testes, declarar arquivos, desenhar arquitetura, contratos/dados/UX e Constitution Check CON-01 a CON-09.
4. Copiar [tasks-template](../.specify/templates/tasks-template.md). Decompor tarefas com FR/RN, dependências, arquivos e aceite; preparar registro de verificações e commits.
5. Implementar na ordem das dependências e dentro da fase autorizada. Registrar revisão humana H1/H2; descoberta que muda premissa ativa H3 e retorna à entrevista/spec/plan. Marcar checkbox após evidência, sem confundir checkpoint com aprovação.
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

### Do plano ao código

O plano declara **Technical Context** (linguagem, versões observadas, dependências, testes, plataforma e restrições), **Constitution Check** com evidência e **Project Structure** com motivo para cada arquivo criado/alterado. Não acrescentar tecnologia por conveniência de ferramenta; um critério de sucesso sem origem não pode virar contrato técnico.

Documentos de apoio só quando necessários: `research.md` para alternativas/decisões técnicas; `data-model.md` para dados e migrations; `contracts/` para schemas; `quickstart.md` para roteiro manual. Em mudança pequena, o plano pode conter essas informações ou justificar N/A. A ordem é entrevista/proposta validada → spec → plan → tasks → implement; cada etapa lê a anterior como premissa e registra onde a premissa precisa de decisão.

Tarefa atômica tem uma mudança e verificação pertinentes, sem nova decisão de negócio. Decompor quando misturar regras/decisões ou exigir várias provas independentes; agrupar fragmentos que apenas repetem comando sem critério novo. Rastreabilidade por regra torna o corte revisável, sem exigir uma tarefa/commit por linha ou supor uma capacidade fixa do agente.

Para nova regra funcional, escrever/reusar um cenário que discrimine o comportamento e observar a falha pertinente antes da correção quando possível. Registrar comando, motivo da falha e resultado após implementar; falha de ambiente não prova regra ausente. Teste que já existe/passa protege regressão, mas sozinho não comprova comportamento novo. Para texto ou regra já coberta, justificar N/A em vez de fabricar teste vermelho. Esta orientação aplica CON-08 sem alterar a constituição.

Usar comandos existentes, não copiar `npm test` genérico do exemplo: backend em `Backend/java-app1/demo` usa `./mvnw.cmd test` e perfil `-Pintegration` conforme ambiente; frontend principal possui `pnpm run lint`, `typecheck`, `build`, `test:e2e:ci`. Mudança backend exige Docker/health/Swagger/OpenAPI conforme plano. Documento usa links/estrutura/diff/scanner. A prova de cada tarefa deve apontar qual desses comandos ou demonstração atende ao cenário e o que faria a verificação falhar.

### Controle por fases

Cada tarefa tem ID, história `[US1]` quando aplicável, regra/FR, dependência, arquivo, comando de prova e resultado real. `[P]` só marca independência de arquivos/decisões; não cria subagente ou autorização de paralelismo. Fases: Setup, Foundational, US1/MVP, histórias seguintes e Polish. A primeira fatia precisa ser demonstrável isoladamente.

Para executar uma fase, usar o prompt em `docs/ai/prompts-escala.md`. O agente valida checkpoints e pode executar testes para revisão humana; não exige que o humano digite os comandos. Antes de alterar código existente e após o MVP, apresentar artefatos concretos e aguardar revisão. Uma autorização já registrada sobre aquele recorte continua válida. Se o usuário pedir somente uma fase e parar, não prosseguir por resultado verde ou passagem de tempo.

Problema/resultado, issue/spec, tarefas, arquivos/contratos afetados, Constitution Check, comandos/resultados, checks do head, riscos/pendências e rollback. GitHub/CI é autoridade para integração. Documento também aciona todos os jobs do workflow sem filtros por caminho.

Constituição e templates são controles documentais. Não há novo hook ou gate automático de rastreabilidade nesta versão. Automação futura exige issue e validação própria; checklist não impõe automaticamente regras no Git.

## Mudança de princípio

Implementação comum não modifica a constituição. Propor emenda separada com riscos e aprovação dos responsáveis; atualizar versão/histórico, ADRs, templates e specs afetadas. Justificativa unilateral não é exceção aprovada.
