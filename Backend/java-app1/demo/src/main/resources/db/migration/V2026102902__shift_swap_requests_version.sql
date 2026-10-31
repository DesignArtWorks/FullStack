-- Fase 3 (race conditions): controle otimista para decisoes simultaneas sobre a mesma troca.
do $$
begin
    if to_regclass('public.shift_swap_requests') is not null then
        alter table shift_swap_requests add column if not exists version bigint not null default 0;
    end if;
end $$;
