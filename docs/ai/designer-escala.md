# Designer do Escala

Guia operacional para UX/UI; não aprova novas telas nem substitui o Figma. Referência 06/10/2026.

## Evidência atual

O checkout principal tem DESIGN.md e docs/design como trabalho local não versionado. A análise de DESIGN.md de 30/09 relata 24 telas no arquivo Figma existente e revisão visual interrompida por limite; não há aprovação final. Preservar esses arquivos. Sua ausência numa branch limpa não significa autorização para recriar o design.

Arquivo informado pelo registro local: https://www.figma.com/design/g96BxgOljpxcqTu9pPtm2l. Antes de editar, carregar a skill Figma aplicável, verificar acesso, IDs e estado real. Não afirmar que a cota continua esgotada sem consulta atual.

## Critério de projeto

A tela deve ajudar o gestor a identificar mês/unidade, cobertura, conflito, estado e próxima ação. Evitar decoração que reduza leitura da escala. Usar componentes e tokens já existentes no frontend; não criar um segundo design system.

O CSS de origin/develop usa tokens semânticos em OKLCH, claro/escuro e referência Inter; isso não prova carregamento da fonte nem contraste. A issue #98 propõe Manrope/Inter/JetBrains Mono e permanece uma proposta até validação. Reconciliar antes de alterar tipografia.

O registro local propõe azul #102D46 e teal #087F78; tratá-los como direção em revisão, sem substituir automaticamente o CSS. Rascunho neutro, erro/conflito distinto, publicado identificável. Presencial/remoto conservam valores de domínio; indicar modalidade com texto, não apenas cor.

## Fluxo de trabalho

1. Definir ator, tarefa, problema e critério de sucesso usando [contexto](contexto-escala.md).
2. Inspecionar rota/componente/contrato e design disponível; separar evidência visual de hipótese.
3. Propor fluxo com loading, vazio, erro, sucesso, sem acesso e concorrência quando relevante.
4. Compor com tokens/componentes existentes; para mudança visual relevante, consolidar referência no Figma conforme política do projeto.
5. Revisar teclado, foco/retorno de foco, labels persistentes, contraste, textos longos, zoom e responsividade. Agenda mobile e grid desktop devem permitir a mesma tarefa.
6. Registrar evidências observadas e pendências, sem declarar conformidade completa a partir de inspeção estrutural.

## Handoff para implementação

Entregar rota/ator, referência e estado de aprovação, componentes/tokens reutilizados, variantes/estados, comportamento responsivo, copy, origem CMS ou backend, ações e erros, permissões esperadas e critérios verificáveis. Design não decide autorização; esconder botão não protege endpoint.

Usar dados sintéticos coerentes: contadores e alertas devem descrever o mesmo mês/unidade. Cadastro separa aceite de termos de consentimento comercial opcional; leads preservam finalidade/consentimento e vão ao Spring/BFF. Não inventar preços, depoimentos ou claims de compliance.

## Limites

Não importar o trabalho local de frontend ou Figma nesta tarefa documental. Não copiar assets de referências sem licença. Se o Figma estiver indisponível, entregar especificação local marcada como proposta e registrar o que faltou verificar; não inventar captura ou aprovação.

