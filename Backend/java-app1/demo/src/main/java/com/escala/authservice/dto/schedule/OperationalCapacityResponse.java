package com.escala.authservice.dto.schedule;

import com.escala.authservice.entity.OperationalCapacity;

import java.time.LocalTime;
import java.util.UUID;

/** Capacidade operacional exposta pela API, sem a entidade de persistencia nem a empresa. */
public record OperationalCapacityResponse(
        UUID id,
        UUID targetId,
        String targetType,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer minEmployeesRequired,
        boolean active
) {

    public static OperationalCapacityResponse from(OperationalCapacity capacity) {
        return new OperationalCapacityResponse(
                capacity.getId(),
                capacity.getTargetId(),
                capacity.getTargetType(),
                capacity.getDayOfWeek(),
                capacity.getStartTime(),
                capacity.getEndTime(),
                capacity.getMinEmployeesRequired(),
                capacity.isActive()
        );
    }
}
