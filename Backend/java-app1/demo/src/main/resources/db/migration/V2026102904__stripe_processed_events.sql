-- Fase 4 (idempotencia): eventos do Stripe ja tratados e ordem dos eventos por assinatura.
create table if not exists stripe_processed_events (
    event_id varchar(255) primary key,
    event_type varchar(255) not null,
    outcome varchar(32) not null,
    detail varchar(500),
    processed_at timestamptz not null default now()
);

do $$
begin
    if to_regclass('public.subscriptions') is not null then
        alter table subscriptions add column if not exists last_event_created bigint;
    end if;
end $$;
