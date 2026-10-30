package com.escala.authservice.dto.schedule;

import com.escala.authservice.entity.WorkShift;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Escala exposta pela API: apenas o necessario, sem a entidade de persistencia nem dados da empresa. */
public record ShiftResponse(
        UUID id,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        String workMode,
        String padraoEscala,
        String notes,
        Long version,
        EmployeeRef employee
) {

    public record EmployeeRef(UUID id, String fullName) {
    }

    public static ShiftResponse from(WorkShift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getStatus() == null ? null : shift.getStatus().name(),
                shift.getWorkMode() == null ? null : shift.getWorkMode().name(),
                shift.getPadraoEscala() == null ? null : shift.getPadraoEscala().name(),
                shift.getNotes(),
                shift.getVersion(),
                shift.getEmployee() == null
                        ? null
                        : new EmployeeRef(shift.getEmployee().getId(), shift.getEmployee().getFullName())
        );
    }
}
