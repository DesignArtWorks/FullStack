# Tarefas #103

Gate H1 aprovado por Wemerson; dossiê e Constitution Check em interview.md/plan.md. Checkpoint H2 pendente: não integrar por inferência.

- [x] T001 — inventariar #99/#103, desenvolver recorte e preservar checkout sujo. PROV-103-001/004; base fa9c27e.
- [x] T002 — executar testes existentes e confirmar fluxo tema Spring/BFF. FR-001/003; 7 PASS locais, sem prova runtime.
- [x] T003 — implementar e executar testes HTTP/persistência/admin/API token com banco descartável. RN-SEC103-001/003; FR-001/003; sem skip.
- [x] T004 — se houver falha real, corrigir somente fronteira identidade e reprovar gatilho/controle. FR-002; caso já seguro registrar no_change de runtime.
- [x] T005 — testes/build/typecheck e revisão independente; diffs/evidências e Constitution Check final. Não confundir resultado focal com gate global #99.
- [x] T006 — apresentar MVP e obter H2. Wemerson; bloqueia fase posterior/merge.
- [ ] T007 — PR/checks/integração no destino autorizado, com SHAs reais. Não fechar #99 nem promover main sem gate integral.

T003: 54 negativas de rotas, JWT válido/assinatura inválida, snapshots, admin e API token/publicação. T004: no_change; sem falha runtime demonstrada. T005: 13 testes + quatro cenários/teste pai, build/TS, actionlint, secrets gate e diff aprovados em Node22.23.2 Debian. Revisão externa limitada à descrição do candidato, sem claim de scan de fonte. T006/T007 pendentes, H2 não inferido.

Registro posterior: H2 aprovado por Wemerson em 2026-10-27, resposta literal 'Aprovar MVP e seguir para develop', PR129/head f6149c9. Revisão do patch real/base concluída sem defeito concreto. T007: PR129 publicado; checks/integração develop ainda pendentes. Não promover main nem fechar #99.
