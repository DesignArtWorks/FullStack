# Especificação #103 — identidade final fora do CMS

Estado: recorte H1 aprovado em 2026-10-27; ver interview.md, PROV-103-004. Não declara liberação global #99.

## Problema, ator e valor

O CMS não pode autenticar usuários finais nem usar ID de user-account como identidade users-permissions. A develop contém bloqueio inicial, mas três testes de plugin não provam HTTP, persistência nem compatibilidade editorial/admin.

## US1 — fechamento da fronteira P0

Como mantenedor, quero evidência real do isolamento entre identidade operacional e CMS preservando publicação editorial.

| Regra / origem | Requisito | Aceite/teste |
| --- | --- | --- |
| RN-SEC103-001 / #99 P0-1, CON-01 | FR-001: auth, tema e CRUD legados inacessíveis | SC-001: anônimo e JWT legado não obtêm sessão/dados/mutação por rotas customizadas ou plugin |
| RN-SEC103-002 / #99 P0-1, CON-03 | FR-002: JWT legado válido não autoriza mesmo com IDs coincidentes | SC-002: assinatura válida de fixture, snapshots inalterados e ausência de conteúdo criado |
| RN-SEC103-003 / #103 e H1 | FR-003: preservar administração e conteúdo editorial | SC-003: login admin, CRUD editorial autorizado, API token editorial e leitura pública funcionam |

## Contrato e invariantes

Produtor: Strapi; consumidores permitidos: admin editorial e leitores de conteúdo. Identidade final/tema: NextAuth/BFF → Spring; nenhuma mudança no contrato Spring/BFF. Rotas legadas removidas retornam 404 ou 405 (o catch-all GET do servidor estático produz 405 para métodos não atendidos); rotas editoriais sem autorização retornam 401/403. Erros sem token/PII; nenhuma resposta de auth legada emite JWT. Dados editoriais globais, sem selecionar tenant; Strapi não acessa dados tenant-bound. Não há regra operacional, evento, email ou migration nova. Tentativa negada não grava dados; repetir tentativa permanece negado. Admin/API tokens são credenciais distintas e não podem ser confundidas com JWT final. Fixtures sintéticas em banco descartável; não apagar dados legados reais. Falha de boot/banco/teste deve falhar a execução, sem skip.

## Escopo e limites

Somente testes, configuração necessária à execução dos testes e bloqueio de identidade se falha real for demonstrada. Não remover schema legado, migrar dados pessoais, alterar outros filhos da #99 nem promover main. API REST Spring não muda: OpenAPI manual não requer alteração. UI não muda: estados visuais N/A; tema permanece autorizado por Spring.
