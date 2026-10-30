-- Fase 3 (race conditions): nome de posto de trabalho unico por empresa, sem diferenciar maiusculas.
-- A checagem existsByCompanyAndNameIgnoreCase + insert na aplicacao nao e atomica.
do $$
begin
    if to_regclass('public.work_posts') is not null then
        if exists (
            select 1 from work_posts
            group by company_id, lower(name)
            having count(*) > 1
        ) then
            raise exception 'Existem postos de trabalho com nome duplicado por empresa em work_posts. '
                'Renomeie ou remova as duplicatas manualmente antes de aplicar esta migration.';
        end if;

        create unique index if not exists uq_work_posts_company_name_ci
            on work_posts (company_id, lower(name));
    end if;
end $$;
