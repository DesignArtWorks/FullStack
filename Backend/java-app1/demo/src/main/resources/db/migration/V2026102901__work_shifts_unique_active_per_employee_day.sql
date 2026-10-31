-- Fase 3 (race conditions): um funcionario nao pode ter duas escalas ativas no mesmo dia.
-- A checagem existsBy... + insert na aplicacao nao e atomica; o indice torna a regra garantida.
do $$
begin
    if to_regclass('public.work_shifts') is not null then
        if exists (
            select 1 from work_shifts
            where status is distinct from 'CANCELLED'
            group by employee_id, shift_date
            having count(*) > 1
        ) then
            raise exception 'Existem escalas ativas duplicadas (employee_id, shift_date) em work_shifts. '
                'Resolva as duplicatas manualmente (cancele as repetidas) antes de aplicar esta migration.';
        end if;

        create unique index if not exists uq_work_shifts_active_employee_day
            on work_shifts (employee_id, shift_date)
            where status is distinct from 'CANCELLED';
    end if;
end $$;
