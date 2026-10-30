-- Fase 4 (idempotencia): repeticoes e duplo clique nao podem duplicar efeitos.

-- Um lead por e-mail, sem diferenciar maiusculas. A aplicacao ja faz buscar-ou-atualizar,
-- mas isso nao e atomico entre requisicoes simultaneas.
do $$
begin
    if to_regclass('public.marketing_leads') is not null then
        if exists (
            select 1 from marketing_leads
            group by lower(email)
            having count(*) > 1
        ) then
            raise exception 'Existem leads com e-mail duplicado em marketing_leads. '
                'Una ou remova as duplicatas manualmente antes de aplicar esta migration.';
        end if;

        create unique index if not exists uq_marketing_leads_email_ci
            on marketing_leads (lower(email));
    end if;
end $$;

-- Uma unica solicitacao de troca aberta por escala original.
do $$
begin
    if to_regclass('public.shift_swap_requests') is not null then
        if exists (
            select 1 from shift_swap_requests
            where status in ('PENDING', 'COLLEAGUE_APPROVED')
            group by original_shift_id
            having count(*) > 1
        ) then
            raise exception 'Existem solicitacoes de troca abertas duplicadas para a mesma escala em shift_swap_requests. '
                'Cancele as repetidas manualmente antes de aplicar esta migration.';
        end if;

        create unique index if not exists uq_shift_swap_open_per_shift
            on shift_swap_requests (original_shift_id)
            where status in ('PENDING', 'COLLEAGUE_APPROVED');
    end if;
end $$;
