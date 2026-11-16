# Spec — revalidação SCA #99

Issue #99, relacionada #108. Dossiê: [interview.md](interview.md). Data: 2026-11-16.

## Problema e história

US1: o mantenedor precisa comprovar o estado atual das dependências; CI antigo e exceções temporárias não comprovam ausência atual de bloqueios. Revalidar Maven/CMS/frontend oficial antes dos demais gates.

## Requisitos e critérios

| ID | Regra e origem | Aceite |
| --- | --- | --- |
| FR-001 / RN-SCA99-001 | P01: consultar versões/advisories e executar SCA dos três componentes | SBOM Maven resolvido; locks npm/pnpm; scanner fixado pelo CI com DB atual, incluindo dev, sem supressões na baseline |
| FR-002 / RN-SCA99-002 | P01: corrigir bloqueios com patches compatíveis | Manifest/lock regenerado se necessário; nenhum bypass, upgrade major incompatível ou aceite prorrogado implicitamente |
| FR-003 / RN-SCA99-003 | P02: reportar resultados e limites | Evidências por comando/componente/SHA, sem declarar #106/#109/#99 concluídas antes de seus próprios aceites |

## Limites

Nenhuma mudança REST, domínio, tenant, dados, UI ou infraestrutura produtiva prevista. Não enviar secrets para serviços externos. Comunicação Slack autorizada apenas sobre resultados sanitizados; canais existentes identificados por contexto. H2/revisão continua exigida para integração. A investigação pode prosseguir enquanto decisões de outras fatias aguardam resposta.
