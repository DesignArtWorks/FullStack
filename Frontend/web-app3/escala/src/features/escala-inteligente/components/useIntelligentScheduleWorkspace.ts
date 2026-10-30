'use client';

import { startTransition, useMemo, useState } from 'react';
import { endOfWeek, format, parseISO, startOfWeek } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { useRouter } from 'next/navigation';
import { createSchedulingCycle, createSchedulingHoliday, acknowledgeSchedulingAlert, archiveSchedulingCycle, publishSchedulingCycle, replaceSchedulingCycleAssignments, rectifySchedulingCycle, validateSchedulingCycle } from '@/services/intelligent-scheduling.service';
import { ReplaceScheduleCycleAssignmentsInput, ScheduleCycle, ScheduleHolidayType } from '@/core/domain/models/schedule.model';
import { UsuarioEscala } from '@/core/domain/escala/escala.types';
import { toast } from 'sonner';
import {
  ScheduleTemplate,
  preferredLegendByImpact,
  assignmentKey,
  AssignmentDraft,
  EditingCell,
  buildAssignmentDraftMap,
} from './workspace.helpers';
import type { WorkspaceProps } from './workspace.helpers';

export function useIntelligentScheduleWorkspace({
  year,
  month,
  timezone,
  cycleId,
  monthCalendar,
  legends,
  cycle,
  assignments,
  alerts: initialAlerts,
  usuarios,
}: WorkspaceProps) {
  const router = useRouter();
  const [alerts, setAlerts] = useState(initialAlerts);
  const [runningAction, setRunningAction] = useState<string | null>(null);
  const [holidayDate, setHolidayDate] = useState(`${year}-${String(month).padStart(2, '0')}-01`);
  const [holidayName, setHolidayName] = useState('');
  const [holidayType, setHolidayType] = useState<ScheduleHolidayType>('CUSTOM');
  const [employeeQuery, setEmployeeQuery] = useState('');
  const [editingCell, setEditingCell] = useState<EditingCell | null>(null);
  const [cellLegendCode, setCellLegendCode] = useState<string>('');
  const [cellModality, setCellModality] = useState<'PRESENCIAL' | 'REMOTO'>('PRESENCIAL');
  const [assignmentDrafts, setAssignmentDrafts] = useState<Record<string, AssignmentDraft>>(() =>
    buildAssignmentDraftMap(assignments)
  );
  const [copyFromEmployeeId, setCopyFromEmployeeId] = useState<string>('');
  const [copyToEmployeeId, setCopyToEmployeeId] = useState<string>('');
  const [templateEmployeeId, setTemplateEmployeeId] = useState<string>('');
  const [selectedTemplate, setSelectedTemplate] = useState<ScheduleTemplate>('5x2');

  const referenceDate = useMemo(() => new Date(year, month - 1, 1), [year, month]);
  const monthLabel = format(referenceDate, "MMMM 'de' yyyy", { locale: ptBR });
  const editableCycleStatuses: ScheduleCycle['status'][] = ['RASCUNHO', 'EM_VALIDACAO', 'RETIFICADO'];
  const canEditAssignments = Boolean(cycleId && cycle && editableCycleStatuses.includes(cycle.status));

  const assignmentsByDate = useMemo(() => {
    return Object.values(assignmentDrafts).reduce<Record<string, AssignmentDraft[]>>((accumulator, assignment) => {
      const dateKey = assignment.date;
      if (!accumulator[dateKey]) {
        accumulator[dateKey] = [];
      }
      accumulator[dateKey].push(assignment);
      return accumulator;
    }, {});
  }, [assignmentDrafts]);

  const draftAssignmentsList = useMemo(
    () => Object.values(assignmentDrafts).sort((left, right) => left.date.localeCompare(right.date) || left.employeeName.localeCompare(right.employeeName)),
    [assignmentDrafts]
  );

  const filteredUsuarios = useMemo(() => {
    const normalizedQuery = employeeQuery.trim().toLowerCase();
    if (!normalizedQuery) {
      return usuarios;
    }

    return usuarios.filter((usuario) => {
      return [usuario.nome, usuario.email, usuario.cargo, usuario.setorNome]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(normalizedQuery));
    });
  }, [employeeQuery, usuarios]);

  const initialAssignmentFingerprint = useMemo(
    () =>
      assignments
        .map((assignment) => `${assignment.employeeId}|${assignment.date}|${assignment.legendCode}|${assignment.modality}`)
        .sort()
        .join('||'),
    [assignments]
  );

  const draftAssignmentFingerprint = useMemo(
    () =>
      draftAssignmentsList
        .map((assignment) => `${assignment.employeeId}|${assignment.date}|${assignment.legendCode}|${assignment.modality}`)
        .sort()
        .join('||'),
    [draftAssignmentsList]
  );

  const hasPendingAssignmentChanges = initialAssignmentFingerprint !== draftAssignmentFingerprint;

  const holidayCountInMonth = monthCalendar?.days.filter((day) => day.holiday).length ?? 0;
  const workedLegend = preferredLegendByImpact(legends, 'WORKED');
  const restLegend = preferredLegendByImpact(legends, 'REST');
  const initialAssignmentsMap = useMemo(() => buildAssignmentDraftMap(assignments), [assignments]);
  const pendingAlertCount = alerts.filter((alert) => !alert.acknowledged).length;
  const pendingCriticalAlertCount = alerts.filter(
    (alert) => alert.severity === 'CRITICAL' && !alert.acknowledged
  ).length;
  const diffSummary = useMemo(() => {
    const changes: Array<{ key: string; type: 'added' | 'updated' | 'removed'; before?: AssignmentDraft; after?: AssignmentDraft }> = [];
    const allKeys = new Set([...Object.keys(initialAssignmentsMap), ...Object.keys(assignmentDrafts)]);

    allKeys.forEach((key) => {
      const before = initialAssignmentsMap[key];
      const after = assignmentDrafts[key];

      if (!before && after) {
        changes.push({ key, type: 'added', after });
        return;
      }

      if (before && !after) {
        changes.push({ key, type: 'removed', before });
        return;
      }

      if (
        before &&
        after &&
        (before.legendCode !== after.legendCode || before.modality !== after.modality)
      ) {
        changes.push({ key, type: 'updated', before, after });
      }
    });

    return {
      added: changes.filter((change) => change.type === 'added'),
      updated: changes.filter((change) => change.type === 'updated'),
      removed: changes.filter((change) => change.type === 'removed'),
      all: changes.sort((left, right) => {
        const leftDate = (left.after?.date ?? left.before?.date ?? '');
        const rightDate = (right.after?.date ?? right.before?.date ?? '');
        return leftDate.localeCompare(rightDate);
      }),
    };
  }, [assignmentDrafts, initialAssignmentsMap]);

  async function runAction(actionKey: string, action: () => Promise<void>) {
    setRunningAction(actionKey);
    try {
      await action();
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Nao foi possivel concluir esta acao.';
      toast.error(message);
    } finally {
      setRunningAction(null);
    }
  }

  function navigateToMonth(nextDate: Date) {
    const params = new URLSearchParams();
    params.set('year', String(nextDate.getFullYear()));
    params.set('month', String(nextDate.getMonth() + 1));
    params.set('timezone', timezone);
    startTransition(() => {
      router.push(`/dashboard/escala/inteligente?${params.toString()}`);
    });
  }

  function openCellEditor(employee: UsuarioEscala, date: string) {
    const currentAssignment = assignmentDrafts[assignmentKey(employee.id, date)];
    setEditingCell({
      employeeId: employee.id,
      employeeName: employee.nome,
      date,
    });
    setCellLegendCode(currentAssignment?.legendCode ?? legends[0]?.code ?? '');
    setCellModality(currentAssignment?.modality ?? 'PRESENCIAL');
  }

  function closeCellEditor() {
    setEditingCell(null);
    setCellLegendCode('');
    setCellModality('PRESENCIAL');
  }

  function saveCellAssignment() {
    if (!editingCell || !cellLegendCode) {
      toast.error('Selecione uma legenda para a atribuicao.');
      return;
    }

    setAssignmentDrafts((current) => ({
      ...current,
      [assignmentKey(editingCell.employeeId, editingCell.date)]: {
        employeeId: editingCell.employeeId,
        employeeName: editingCell.employeeName,
        date: editingCell.date,
        legendCode: cellLegendCode,
        modality: cellModality,
      },
    }));
    closeCellEditor();
  }

  function applyAssignmentToWholeWeek() {
    if (!editingCell || !cellLegendCode) {
      toast.error('Selecione uma legenda para preencher a semana.');
      return;
    }

    const weekStart = startOfWeek(parseISO(editingCell.date), { weekStartsOn: 1 });
    const weekEnd = endOfWeek(parseISO(editingCell.date), { weekStartsOn: 1 });
    const weekDays = monthCalendar?.days.filter((day) => {
      const current = parseISO(day.date);
      return current >= weekStart && current <= weekEnd;
    }) ?? [];

    setAssignmentDrafts((current) => {
      const nextDrafts = { ...current };
      weekDays.forEach((day) => {
        nextDrafts[assignmentKey(editingCell.employeeId, day.date)] = {
          employeeId: editingCell.employeeId,
          employeeName: editingCell.employeeName,
          date: day.date,
          legendCode: cellLegendCode,
          modality: cellModality,
        };
      });
      return nextDrafts;
    });

    toast.success('Semana preenchida.');
    closeCellEditor();
  }

  function clearCellAssignment() {
    if (!editingCell) return;
    setAssignmentDrafts((current) => {
      const nextDrafts = { ...current };
      delete nextDrafts[assignmentKey(editingCell.employeeId, editingCell.date)];
      return nextDrafts;
    });
    closeCellEditor();
  }

  function handleCopyAssignmentsBetweenEmployees() {
    if (!copyFromEmployeeId || !copyToEmployeeId) {
      toast.error('Selecione origem e destino para copiar a escala.');
      return;
    }

    if (copyFromEmployeeId === copyToEmployeeId) {
      toast.error('Origem e destino precisam ser diferentes.');
      return;
    }

    const sourceEmployee = usuarios.find((usuario) => usuario.id === copyFromEmployeeId);
    const targetEmployee = usuarios.find((usuario) => usuario.id === copyToEmployeeId);

    if (!sourceEmployee || !targetEmployee) {
      toast.error('Nao foi possivel localizar os colaboradores selecionados.');
      return;
    }

    setAssignmentDrafts((current) => {
      const nextDrafts = { ...current };

      Object.keys(nextDrafts).forEach((key) => {
        if (key.startsWith(`${copyToEmployeeId}::`)) {
          delete nextDrafts[key];
        }
      });

      Object.values(current)
        .filter((draft) => draft.employeeId === copyFromEmployeeId)
        .forEach((draft) => {
          nextDrafts[assignmentKey(copyToEmployeeId, draft.date)] = {
            employeeId: copyToEmployeeId,
            employeeName: targetEmployee.nome,
            date: draft.date,
            legendCode: draft.legendCode,
            modality: draft.modality,
          };
        });

      return nextDrafts;
    });

    toast.success(`Escala mensal copiada de ${sourceEmployee.nome} para ${targetEmployee.nome}.`);
  }

  function handleApplyTemplate() {
    if (!templateEmployeeId) {
      toast.error('Selecione um colaborador para aplicar o template.');
      return;
    }

    if (!monthCalendar || !workedLegend || !restLegend) {
      toast.error('Legendas de trabalho e descanso precisam estar disponiveis.');
      return;
    }

    const targetEmployee = usuarios.find((usuario) => usuario.id === templateEmployeeId);
    if (!targetEmployee) {
      toast.error('Colaborador do template nao encontrado.');
      return;
    }

    setAssignmentDrafts((current) => {
      const nextDrafts = { ...current };

      Object.keys(nextDrafts).forEach((key) => {
        if (key.startsWith(`${templateEmployeeId}::`)) {
          delete nextDrafts[key];
        }
      });

      monthCalendar.days.forEach((day, index) => {
        const parsedDate = parseISO(day.date);
        const weekday = parsedDate.getDay();
        const isSunday = weekday === 0;
        const isSaturday = weekday === 6;

        let shouldWork = false;
        if (selectedTemplate === '5x2') {
          shouldWork = !day.holiday && !isSaturday && !isSunday;
        } else if (selectedTemplate === '6x1') {
          shouldWork = !day.holiday && !isSunday;
        } else if (selectedTemplate === '12x36') {
          shouldWork = !day.holiday && index % 2 === 0;
        }

        const legend = shouldWork ? workedLegend : restLegend;
        const modality = shouldWork && selectedTemplate === '5x2' ? 'PRESENCIAL' : shouldWork ? 'REMOTO' : 'PRESENCIAL';

        nextDrafts[assignmentKey(templateEmployeeId, day.date)] = {
          employeeId: templateEmployeeId,
          employeeName: targetEmployee.nome,
          date: day.date,
          legendCode: legend.code,
          modality,
        };
      });

      return nextDrafts;
    });

    toast.success(`Template ${selectedTemplate} aplicado para ${targetEmployee.nome}.`);
  }

  async function handleSaveAssignments() {
    if (!cycleId) return;

    const payload: ReplaceScheduleCycleAssignmentsInput = {
      assignments: draftAssignmentsList.map((assignment) => ({
        employeeId: assignment.employeeId,
        date: assignment.date,
        legendCode: assignment.legendCode,
        modality: assignment.modality,
      })),
    };

    await runAction('save-assignments', async () => {
      const saved = await replaceSchedulingCycleAssignments(cycleId, payload);
      if (!saved) {
        toast.error('Nao foi possivel salvar as atribuicoes do ciclo.');
        return;
      }

      toast.success('Atribuicoes atualizadas.');
      router.refresh();
    });
  }

  async function handleCreateCycle() {
    await runAction('create-cycle', async () => {
      const created = await createSchedulingCycle({ year, month, timezone });
      if (!created?.id) {
        toast.error('Nao foi possivel criar o ciclo mensal.');
        return;
      }

      toast.success('Ciclo mensal criado.');
      const params = new URLSearchParams({
        year: String(year),
        month: String(month),
        timezone,
        cycleId: created.id,
      });
      startTransition(() => {
        router.push(`/dashboard/escala/inteligente?${params.toString()}`);
      });
    });
  }

  async function handleValidateCycle() {
    if (!cycleId) return;
    await runAction('validate-cycle', async () => {
      const nextAlerts = await validateSchedulingCycle(cycleId);
      setAlerts(nextAlerts);
      toast.success('Validacao concluida.');
      router.refresh();
    });
  }

  async function handlePublishCycle() {
    if (!cycleId) return;
    await runAction('publish-cycle', async () => {
      const published = await publishSchedulingCycle(cycleId);
      if (!published) {
        toast.error('Nao foi possivel publicar o ciclo.');
        return;
      }
      toast.success('Ciclo publicado.');
      router.refresh();
    });
  }

  async function handleRectifyCycle() {
    if (!cycleId) return;
    await runAction('rectify-cycle', async () => {
      const rectified = await rectifySchedulingCycle(cycleId);
      if (!rectified) {
        toast.error('Nao foi possivel abrir a retificacao.');
        return;
      }
      toast.success('Retificacao iniciada.');
      router.refresh();
    });
  }

  async function handleArchiveCycle() {
    if (!cycleId) return;
    await runAction('archive-cycle', async () => {
      const archived = await archiveSchedulingCycle(cycleId);
      if (!archived) {
        toast.error('Nao foi possivel arquivar o ciclo.');
        return;
      }
      toast.success('Ciclo arquivado.');
      router.refresh();
    });
  }

  async function handleAcknowledgeAlert(alertId: string) {
    if (!cycleId) return;
    await runAction(`ack-${alertId}`, async () => {
      const response = await acknowledgeSchedulingAlert(cycleId, alertId);
      if (!response) {
        toast.error('Nao foi possivel registrar a ciencia do alerta.');
        return;
      }
      setAlerts((current) =>
        current.map((alert) =>
          alert.id === alertId
            ? { ...alert, acknowledged: true, acknowledgedAt: response.acknowledgedAt }
            : alert
        )
      );
      toast.success('Ciencia registrada.');
    });
  }

  async function handleCreateHoliday() {
    if (!holidayDate || !holidayName.trim()) {
      toast.error('Informe data e nome do feriado.');
      return;
    }

    await runAction('create-holiday', async () => {
      const created = await createSchedulingHoliday({
        date: holidayDate,
        name: holidayName.trim(),
        type: holidayType,
      });

      if (!created) {
        toast.error('Nao foi possivel criar o feriado.');
        return;
      }

      toast.success('Feriado cadastrado.');
      setHolidayName('');
      router.refresh();
    });
  }

  return {
    alerts,
    setAlerts,
    runningAction,
    setRunningAction,
    holidayDate,
    setHolidayDate,
    holidayName,
    setHolidayName,
    holidayType,
    setHolidayType,
    employeeQuery,
    setEmployeeQuery,
    editingCell,
    setEditingCell,
    cellLegendCode,
    setCellLegendCode,
    cellModality,
    setCellModality,
    assignmentDrafts,
    setAssignmentDrafts,
    copyFromEmployeeId,
    setCopyFromEmployeeId,
    copyToEmployeeId,
    setCopyToEmployeeId,
    templateEmployeeId,
    setTemplateEmployeeId,
    selectedTemplate,
    setSelectedTemplate,
    router,
    referenceDate,
    monthLabel,
    canEditAssignments,
    assignmentsByDate,
    draftAssignmentsList,
    filteredUsuarios,
    initialAssignmentFingerprint,
    draftAssignmentFingerprint,
    hasPendingAssignmentChanges,
    holidayCountInMonth,
    workedLegend,
    restLegend,
    initialAssignmentsMap,
    pendingAlertCount,
    pendingCriticalAlertCount,
    diffSummary,
    runAction,
    navigateToMonth,
    openCellEditor,
    closeCellEditor,
    saveCellAssignment,
    applyAssignmentToWholeWeek,
    clearCellAssignment,
    handleCopyAssignmentsBetweenEmployees,
    handleApplyTemplate,
    handleSaveAssignments,
    handleCreateCycle,
    handleValidateCycle,
    handlePublishCycle,
    handleRectifyCycle,
    handleArchiveCycle,
    handleAcknowledgeAlert,
    handleCreateHoliday,
  };
}
