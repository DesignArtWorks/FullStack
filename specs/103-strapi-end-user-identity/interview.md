# Retomada #103 — identidade final fora do CMS

Data: 2026-10-27. Responsável: Wemerson. Filho P0 da #99.

## 1. As três canônicas

O que: provar em Strapi real a desativação de auth/tema/CRUD legados e rejeição de JWT legado com IDs coincidentes. Para quem: mantenedor e usuários finais do Escala; preservar CMS editorial/admin. Sucesso: negativos reais e controles positivos aprovados sem tocar dados existentes; não equivale a liberar #99.

## 2. Proveniência e autorização

| ID | Classe | Fonte/alcance |
| --- | --- | --- |
| PROV-103-001 | RESTRIÇÃO | AGENTS fornecido pelo usuário e CON-01/03: Spring/BFF é dono da identidade final |
| PROV-103-002 | EVIDÊNCIA | #99/#103 consultadas em 2026-10-27: P0 aberto; não é prova runtime |
| PROV-103-003 | EVIDÊNCIA | develop fa9c27e33d7195928a65ba62540753ec0310d077 já remove rotas/substitui JWT; sete testes locais passaram em Node24, sem boot real |
| PROV-103-004 | DECISÃO | Wemerson nesta conversa: “Aprovar esse recorte da #103”, em resposta ao H1 abaixo |

H1: validar em Strapi real login/registro/recuperação e tema legados inacessíveis, JWT legado sem mutação mesmo com IDs coincidentes, admin/conteúdo editorial preservados; ajustar apenas testes e bloqueio de identidade se houver falha comprovada.

Dossiê de retomada com aprovação técnica explícita; não afirma nova entrevista completa em cinco fases/seis categorias. Reutiliza arquitetura fornecida e H1 concreto; não inventa respostas nem transforma testes verdes em H2.

## 3. Termos observáveis

Strapi real = processo 5.56.0 + PostgreSQL descartável + HTTP. IDs coincidentes = fixtures nos dois modelos e snapshots antes/depois. Admin/editorial = login e escrita autorizados, leitura pública e API token editorial.

## 4. Riscos e perspectivas alternativas

Remover plugin inteiro quebraria permissões públicas/admin. Testar só JWT inválido não prova rejeição do legado: assinar fixture com chave descartável. Bloqueio amplo de Bearer pode quebrar API tokens editoriais: controle positivo obrigatório. Banco operacional existente não será usado para fixtures.

## 5. Abertas

H1 autorizado por PROV-103-004. H2: Wemerson revisa MVP antes de merge/fase posterior. Gate global #99, demais filhos e promoção main permanecem pendentes e fora do recorte.

H2 em 2026-10-27: Wemerson respondeu “Aprovar MVP e seguir para develop” ao checkpoint do PR129/commit f6149c9. Revisão fonte do patch real/base sem defeito concreto. H2 autorizado para checks e integração develop condicionada a sucesso; produção/main e #99 continuam bloqueadas. Este registro posterior substitui apenas a pendência H2 acima.
