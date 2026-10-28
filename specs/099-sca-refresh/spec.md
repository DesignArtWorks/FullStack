# Especificação — SCA #99

Spec099-sca-refresh; H1 aprovado PROV-SCA002 em interview.md, 2026-10-27.

## Problema/ator/valor e US1

Mantenedor precisa desbloquear dependências do preflight mantendo CMS/admin/editorial e validação existentes. O relatório12 do CMS bloqueia Required Gate; não é causado pelo notice de versão do Trivy.

| Regra de origem | Requisito | Sucesso verificável |
| --- | --- | --- |
| RN-SCA99-001 / #99 P1-SCA, CON-08 | FR-001 atualizar versões afetadas com patch compatível | SC-001 lock instalado e SCA atual sem High/Critical não aceitos; sem claim sobre caminhos não escaneados |
| RN-SCA99-002 / AGENTS contratos/compatibilidade | FR-002 preservar sanitização, Markdown, HTTP, schema/configuração e boot/admin | SC-002 gatilhos negativos proporcionais + controles legítimos, build e Strapi real passam |
| RN-SCA99-003 / H1/CON-09 | FR-003 não ocultar Zod3 nem inserir exceção sem decisão | SC-003 evidência rastreável, item remanescente bloqueia gate enquanto não disposto formalmente |

Sem novo endpoint, tenant, evento, dado operacional ou contrato Spring/BFF. Dependências editoriais e ferramentas de build; sem seleção de tenant. Fixtures sintéticas e banco descartável; nenhum dado real alterado. Nenhuma migration, email ou efeito operacional novo. Erros/testes não devem imprimir segredo real. Concorrência operacional N/A. Retry de teste deve usar estado isolado. Frontend preservado salvo lock/manifest necessário a achado comprovado. OpenAPI Spring N/A, sem mudança REST.

Exclusões: atualizar Trivy por notice, major arbitrário, mascarar CVE via import legado em versão nova, waiver genérico, promoção main, novo plugin/runtime. Zod3 exige decisão separada se não houver correção compatível.
