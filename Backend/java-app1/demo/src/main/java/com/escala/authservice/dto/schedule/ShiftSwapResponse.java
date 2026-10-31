package com.escala.authservice.dto.schedule;

import com.escala.authservice.entity.ShiftSwapRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Solicitacao de troca exposta pela API, sem a entidade de persistencia. */
public record ShiftSwapResponse(
        UUID id,
        ShiftResponse.EmployeeRef requester,
        ShiftResponse originalShift,
        LocalDate compensationDate,
        String comments,
        String adminComments,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime decidedAt,
        Long version
) {

    public static ShiftSwapResponse from(ShiftSwapRequest swap) {
        return new ShiftSwapResponse(
                swap.getId(),
                swap.getRequester() == null
                        ? null
                        : new ShiftResponse.EmployeeRef(swap.getRequester().getId(), swap.getRequester().getFullName()),
                swap.getOriginalShift() == null ? null : ShiftResponse.from(swap.getOriginalShift()),
                swap.getCompensationDate(),
                swap.getComments(),
                swap.getAdminComments(),
                swap.getStatus() == null ? null : swap.getStatus().name(),
                swap.getCreatedAt(),
                swap.getDecidedAt(),
                swap.getVersion()
        );
    }
}
