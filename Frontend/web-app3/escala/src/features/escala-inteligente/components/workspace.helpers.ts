import { MonthCalendar, ScheduleCycle, ScheduleCycleAssignment, ScheduleCycleCounter, ScheduleHoliday, ScheduleHolidayType, ScheduleLegend, ScheduleValidationAlert } from '@/core/domain/models/schedule.model';
import { UsuarioEscala } from '@/core/domain/escala/escala.types';

export const HOLIDAY_TYPES: { value: ScheduleHolidayType; label: string }[] = [
  { value: 'NATIONAL', label: 'Nacional' },
  { value: 'STATE', label: 'Estadual' },
  { value: 'MUNICIPAL', label: 'Municipal' },
  { value: 'CUSTOM', label: 'Customizado' },
];

export const TEMPLATE_OPTIONS = [
  { value: '5x2', label: 'Template 5x2' },
  { value: '6x1', label: 'Template 6x1' },
  { value: '12x36', label: 'Template 12x36' },
] as const;

export type ScheduleTemplate = (typeof TEMPLATE_OPTIONS)[number]['value'];

export function formatMinutes(minutes: number) {
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  return `${hours}h${String(remainingMinutes).padStart(2, '0')}`;
}

export function formatCycleStatus(status?: string) {
  return status ? status.replaceAll('_', ' ') : 'Sem ciclo';
}

export function getAlertTone(severity: ScheduleValidationAlert['severity']) {
  if (severity === 'CRITICAL') return 'border-red-200 bg-red-50 text-red-900';
  if (severity === 'WARNING') return 'border-amber-200 bg-amber-50 text-amber-900';
  return 'border-sky-200 bg-sky-50 text-sky-900';
}

export function preferredLegendByImpact(legends: ScheduleLegend[], impact: ScheduleLegend['impact']) {
  const preferredCodes =
    impact === 'WORKED'
      ? ['TRAB', 'WORK', 'PRES', 'REM']
      : impact === 'REST'
        ? ['FOLGA', 'DESC', 'REST']
        : impact === 'ABSENCE'
          ? ['FERIAS', 'ATEST', 'AUS']
          : [];

  return (
    preferredCodes
      .map((code) => legends.find((legend) => legend.code.toUpperCase() === code))
      .find(Boolean) ||
    legends.find((legend) => legend.impact === impact) ||
    legends[0] ||
    null
  );
}

export function assignmentKey(employeeId: string, date: string) {
  return `${employeeId}::${date}`;
}

export type AssignmentDraft = {
  employeeId: string;
  employeeName: string;
  date: string;
  legendCode: string;
  modality: 'PRESENCIAL' | 'REMOTO';
};

export type EditingCell = {
  employeeId: string;
  employeeName: string;
  date: string;
};

export function buildAssignmentDraftMap(assignments: ScheduleCycleAssignment[]) {
  return assignments.reduce<Record<string, AssignmentDraft>>((accumulator, assignment) => {
    accumulator[assignmentKey(assignment.employeeId, assignment.date)] = {
      employeeId: assignment.employeeId,
      employeeName: assignment.employeeName,
      date: assignment.date,
      legendCode: assignment.legendCode,
      modality: assignment.modality,
    };
    return accumulator;
  }, {});
}


export type WorkspaceProps = {
  year: number;
  month: number;
  timezone: string;
  cycleId?: string;
  monthCalendar: MonthCalendar | null;
  legends: ScheduleLegend[];
  holidays: ScheduleHoliday[];
  cycle: ScheduleCycle | null;
  assignments: ScheduleCycleAssignment[];
  counters: ScheduleCycleCounter[];
  alerts: ScheduleValidationAlert[];
  usuarios: UsuarioEscala[];
  cycleLoadFailed: boolean;
};
