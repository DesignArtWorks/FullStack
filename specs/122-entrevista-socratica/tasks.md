# Tarefas — Entrevista socrática e fases

Spec: `122-entrevista-socratica` | Issue: #122 | Modo aprovado: por fases
Plano: [plan.md](plan.md) | Origem real: [intake.md](intake.md)
Base: `91484273f9ea473ce38d0c6e3364d62c9a12315b`

## Setup

- [x] **T001 — Conferir e integrar PR #121.** FR-007. Dep.: nenhuma. Artefato: PR/ledger anterior. Prova: oito checks success no run 37790079431, três conversas corrigidas/resolvidas, merge GitHub. Aceite: merged=true em develop, SHA 91484273.
- [x] **T002 — Isolar nova issue e fontes.** FR-001/005. Dep.: T001. Artefatos: issue #122 e branch. Prova: fetch/worktree/status/base. Aceite: docs/issue-122-entrevista-socratica baseada em develop pós-merge, sem alterações locais de UI incluídas.

Checkpoint C1: base e pedido conhecidos; fontes humanas separadas do material didático.

## Foundational

- [x] **T003 — Registrar origem, escopo e plano.** FR-001 a FR-007. Dep.: T002. Arquivos: intake/spec/plan desta pasta. Prova: fontes PROV-001 a PROV-007, decisão humana real de modo e Constitution Check inicial. Aceite: nenhuma entrevista retroativa inventada; arquivos/risco definidos.

H1: N/A nesta adoção — nenhum código existente é alterado, somente Markdown. Nas implementações futuras, H1 exige revisão humana antes do trecho de código existente.

## US1 (P1), MVP documental

- [x] **T004 [US1] — Criar roteiro, exemplo e dossiê.** FR-001/002/003/006. Dep.: T003. Arquivos: docs/ai/entrevista-socratica.md, exemplo e interview-template.md. Prova: revisão do contrato e estrutura de cinco seções. Aceite: uma pergunta, fases/categorias, síntese, quantificação, classificação e fechamento incompleto claros.
- [x] **T005 [US1] — Vincular entradas e templates.** FR-004/005. Dep.: T004. Arquivos: AGENTS, spec-template, plan-template, índice/prompts/guia. Prova: referências/gates e revisão de escopo. Aceite: roteiro localizado antes de implementar e H1/H2/H3 explícitos.
- [x] **T006 [US1] — Decompor execução por fases.** FR-004/005/008. Dep.: T005. Arquivos: tasks-template, plan-template, guia e tasks atual. Prova: fases, formato T/[P]/[US], contexto/estrutura/apoios, dependências, comandos de prova, aceite/resultado e ledger. Aceite: US1 demonstrável, tarefas atômicas e controles humanos separados de checkpoints.
- [x] **T007 [US1] — Atualizar evidência do merge anterior.** FR-007. Dep.: T001/T006. Arquivo: specs/120-governanca-especificacao/tasks.md. Prova: SHA/CI/merge reais. Aceite: T011 concluído; promoção main continua pendente e fora da ordem atual.
- [x] **T008 [US1] — Verificar MVP documental.** FR-001 a FR-008; SC-001 a SC-005. Dep.: T004 a T007. Prova: PowerShell verificou links/estrutura/gates/H1-H3 e checks inicial/final; git diff --cached --check e scanner versionado aprovados em 2026-10-08. Aceite: somente Markdown, runtime N/A justificado.
- [x] **T009 [US1] — Registrar commits/push e PR de revisão.** FR-004/005. Dep.: T008. Prova: commit 6e9679de79ac0d21a46c1d6ad8718eea987aa127 enviado a origin; [PR #123](https://github.com/DesignArtWorks/FullStack/pull/123) aberto como rascunho para develop. Aceite: diff do MVP disponível para Wemerson; revisão H2 e merge pendentes.

Checkpoint C3: roteiro/dossiê/exemplo e integração documental completos, verificações e diff disponíveis.

**H2 — revisão humana do MVP:** responsável Wemerson; estado PENDING. Apresentar artefatos/PR/validações e aguardar revisão antes de integrar a nova política. Commit/PR de revisão registra a fase produzida, não sua aprovação.

H3: nenhuma decisão bloqueadora conhecida; se aparecer, registrar pergunta/dono e parar trecho dependente.

## Polish / integração, após H2

- [ ] **T010 — Registrar decisão e correções solicitadas.** Dep.: H2. Prova: resposta humana e diff pertinente. Aceite: aprovação do recorte ou mudanças requeridas registradas sem inferência.
- [ ] **T011 — Conferir CI/revisão e integrar no destino autorizado.** Dep.: T010 e checks do head. Prova: PR/CI/merge reais. Aceite: develop somente se autorizado; sem bypass e sem promoção main inferida.

## Ledger

| Tarefa | Estado | Evidência | Commit / PR |
| --- | --- | --- | --- |
| T001–T003 | DONE | Merge #121, base/branch e origem/spec/plan | Base 91484273 |
| T004–T007 | DONE | Artefatos do MVP produzidos e enviados | 6e9679de79ac0d21a46c1d6ad8718eea987aa127 |
| T008 | DONE | Links/estrutura/gates/diff/scanner aprovados em 2026-10-08; Constitution Check final com evidência | Resultado local |
| T009 | DONE | Commit/push e PR rascunho para revisão H2 | [PR #123](https://github.com/DesignArtWorks/FullStack/pull/123) |
| H2 / T010–T011 | PENDING | Revisão do MVP / CI / integração | Wemerson / GitHub |

## Validações

| Verificação | Resultado | Evidência / motivo |
| --- | --- | --- |
| Links locais / cinco seções / gates / IDs | PASS | PowerShell: quatorze arquivos com links locais resolvidos; cinco seções; H1/H2/H3; check inicial/final CON-01 a CON-09 |
| Diff / scanner de segredos | PASS | git diff --cached --check sem erro; ./scripts/security/check-versioned-secrets.ps1: Versioned secret scan passed |
| Constitution Check final | PASS local | Evidência por princípio no plan; revisão humana e CI ainda pendentes |
| Testes/lint/typecheck/build/Docker local | N/A | Somente documentos; runtime/contratos inalterados |
| CI novo PR | PENDING | Obrigatório antes de merge, sem filtros por caminho |
| Revisão humana H2 | PENDING | Pedido do usuário: execução por fases com revisão após MVP |

Registrar SHA real do commit atual no PR ou commit posterior, evitando auto-referência impossível.
