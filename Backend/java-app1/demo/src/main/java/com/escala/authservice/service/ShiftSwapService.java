package com.escala.authservice.service;

import com.escala.authservice.dto.CreateShiftSwapRequest;
import com.escala.authservice.dto.DecideShiftSwapRequest;
import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.Role;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.ShiftSwapRequest;
import com.escala.authservice.entity.SwapStatus;
import com.escala.authservice.entity.User;
import com.escala.authservice.entity.WorkShift;
import com.escala.authservice.repository.EmployeeRepository;
import com.escala.authservice.repository.ShiftSwapRequestRepository;
import com.escala.authservice.repository.WorkShiftRepository;
import com.escala.authservice.scheduling.domain.enums.StatusTroca;
import com.escala.authservice.scheduling.domain.model.FluxoTrocaEscala;
import com.escala.authservice.scheduling.domain.model.SolicitacaoTrocaEscala;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Troca de escala em duas etapas (aceite do colega e decisao do gestor). Extraido de
 * ScheduleService sem mudar regras: o fluxo de estados vem de FluxoTrocaEscala.
 */
@Service
@RequiredArgsConstructor
public class ShiftSwapService {
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final ShiftSwapRequestRepository shiftSwapRequestRepository;
    private final AuditLogService auditLogService;
    private final PolicyService policyService;
    private final CurrentUserService currentUserService;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    private Company resolveCompany(String userEmail) {
        Company company = currentUserService.requireCurrentUser(userEmail).getCompany();
        if (company == null) {
            throw new IllegalArgumentException("Usuario ou empresa nao encontrados");
        }
        return company;
    }

    public ShiftSwapRequest requestSwap(CreateShiftSwapRequest request, String userEmail) {
        User currentUser = currentUserService.requireCurrentUser(userEmail);
        Employee currentEmployee = currentEmployeeResolver.resolve(currentUser);
        Employee requester = currentEmployee;
        if (request.getRequesterId() != null && !Objects.equals(request.getRequesterId(), currentEmployee.getId())) {
            policyService.requireCanManageSchedules(currentUser);
            requester = employeeRepository.findById(request.getRequesterId()).orElseThrow();
        }
        
        Company company = resolveCompany(userEmail);
        if (!Objects.equals(requester.getCompany().getId(), company.getId())) {
             throw new org.springframework.security.access.AccessDeniedException("Nao autorizado");
        }
        
        return requestSwap(request, requester);
    }

    private ShiftSwapRequest requestSwap(CreateShiftSwapRequest request, Employee requester) {
        WorkShift originalShift = workShiftRepository.findById(request.getOriginalShiftId()).orElseThrow();

        if (!Objects.equals(originalShift.getEmployee().getCompany().getId(), requester.getCompany().getId())) {
             throw new org.springframework.security.access.AccessDeniedException("Escala nao pertence a sua empresa");
        }

        if (shiftSwapRequestRepository.existsByOriginalShiftIdAndStatusIn(
                originalShift.getId(), List.of(SwapStatus.PENDING, SwapStatus.COLLEAGUE_APPROVED))) {
            throw new IllegalStateException("Ja existe uma solicitacao de troca aberta para esta escala");
        }

        new SolicitacaoTrocaEscala(
                requester.getId(),
                originalShift.getEmployee().getId(),
                originalShift.getShiftDate(),
                request.getCompensationDate()
        ).validarSolicitacao(LocalDate.now());

        ShiftSwapRequest saved = shiftSwapRequestRepository.save(ShiftSwapRequest.builder()
                .requester(requester)
                .originalShift(originalShift)
                .compensationDate(request.getCompensationDate())
                .comments(request.getComments())
                .status(SwapStatus.PENDING)
                .build());
        auditLogService.record(
                requester.getEmail(),
                "SHIFT_SWAP_REQUESTED",
                "ShiftSwapRequest",
                saved.getId(),
                "Escala original=" + originalShift.getId() + "; compensacao=" + request.getCompensationDate()
        );
        return saved;
    }

    @Transactional
    public ShiftSwapRequest decideSwap(UUID id, DecideShiftSwapRequest request, String userEmail) {
        Company company = resolveCompany(userEmail);
        User user = currentUserService.requireCurrentUser(userEmail);
        policyService.requireCanManageSchedules(user);
        
        ShiftSwapRequest swap = shiftSwapRequestRepository.findById(id).orElseThrow();
        
        boolean isSystemAdmin = user.getRoles() != null && user.getRoles().stream().map(Role::getName).anyMatch(r -> r.equals("SYSTEM_ADMIN"));
        if (!isSystemAdmin && !Objects.equals(swap.getRequester().getCompany().getId(), company.getId())) {
             throw new org.springframework.security.access.AccessDeniedException("Nao autorizado");
        }

        if (swap.getStatus() != SwapStatus.PENDING && swap.getStatus() != SwapStatus.COLLEAGUE_APPROVED) {
            throw new IllegalStateException("Solicitacao ja decidida");
        }

        FluxoTrocaEscala fluxo = new FluxoTrocaEscala(toDomainStatus(swap.getStatus()));
        StatusTroca statusGestor = fluxo.decidirPeloGestor(request.isApproved());
        swap.setStatus(toPersistenceStatus(statusGestor));
        swap.setAdminComments(request.getAdminComments());
        swap.setDecidedAt(OffsetDateTime.now());

        if (request.isApproved() && swap.getCompensationDate() != null) {
            WorkShift originalShift = swap.getOriginalShift();
            if (!workShiftRepository.existsByEmployeeIdAndShiftDateAndStatusNot(
                    swap.getRequester().getId(),
                    swap.getCompensationDate(),
                    ShiftStatus.CANCELLED
            )) {
                workShiftRepository.save(WorkShift.builder()
                        .employee(swap.getRequester())
                        .shiftDate(swap.getCompensationDate())
                        .startTime(originalShift.getStartTime())
                        .endTime(originalShift.getEndTime())
                        .status(ShiftStatus.SCHEDULED)
                        .workMode(originalShift.getWorkMode())
                        .notes("Compensacao da troca #" + swap.getId())
                        .build());
            }
            originalShift.setStatus(ShiftStatus.CANCELLED);
            workShiftRepository.save(originalShift);
            if (request.isEffective()) {
                swap.setStatus(toPersistenceStatus(fluxo.efetivar()));
            }
        }

        ShiftSwapRequest saved = shiftSwapRequestRepository.save(swap);
        auditLogService.record(
                userEmail,
                request.isApproved() ? "SHIFT_SWAP_APPROVED" : "SHIFT_SWAP_REJECTED",
                "ShiftSwapRequest",
                saved.getId(),
                "Status=" + saved.getStatus() + "; comentarios=" + request.getAdminComments()
        );
        return saved;
    }

    @Transactional
    public ShiftSwapRequest approveByColleague(UUID id, String userEmail) {
        Company company = resolveCompany(userEmail);
        User user = currentUserService.requireCurrentUser(userEmail);
        ShiftSwapRequest swap = shiftSwapRequestRepository.findById(id).orElseThrow();
        
        boolean isSystemAdmin = user.getRoles() != null && user.getRoles().stream().map(Role::getName).anyMatch(r -> r.equals("SYSTEM_ADMIN"));
        if (!isSystemAdmin && !Objects.equals(swap.getRequester().getCompany().getId(), company.getId())) {
             throw new org.springframework.security.access.AccessDeniedException("Nao autorizado");
        }

        if (!isSystemAdmin && Objects.equals(swap.getRequester().getEmail(), user.getEmail())) {
            throw new org.springframework.security.access.AccessDeniedException("Nao e permitido ao solicitante aprovar a propria troca como colega");
        }
        if (!isSystemAdmin && !policyService.canManageSchedules(user)) {
            Employee approver = currentEmployeeResolver.resolve(user);
            if (approver.getSector() == null
                    || swap.getRequester().getSector() == null
                    || !Objects.equals(approver.getSector().getId(), swap.getRequester().getSector().getId())) {
                throw new org.springframework.security.access.AccessDeniedException("A aprovacao do colega exige colaborador do mesmo setor");
            }
        }

        FluxoTrocaEscala fluxo = new FluxoTrocaEscala(toDomainStatus(swap.getStatus()));
        swap.setStatus(toPersistenceStatus(fluxo.aprovarPeloColega()));
        ShiftSwapRequest saved = shiftSwapRequestRepository.save(swap);
        auditLogService.record(
                userEmail,
                "SHIFT_SWAP_COLLEAGUE_APPROVED",
                "ShiftSwapRequest",
                saved.getId(),
                "Status=" + saved.getStatus()
        );
        return saved;
    }

    public List<ShiftSwapRequest> swapRequests(String userEmail) {
        Company company = resolveCompany(userEmail);
        User user = currentUserService.requireCurrentUser(userEmail);
        if (!policyService.canManageSchedules(user)) {
            return shiftSwapRequestRepository.findByRequesterIdOrderByCreatedAtDesc(currentEmployeeResolver.resolve(user).getId());
        }
        return shiftSwapRequestRepository.findByRequesterCompanyIdOrderByCreatedAtDesc(company.getId());
    }

    private StatusTroca toDomainStatus(SwapStatus status) {
        return switch (status) {
            case PENDING -> StatusTroca.SOLICITADO;
            case COLLEAGUE_APPROVED -> StatusTroca.APROVADO_PELO_COLEGA;
            case APPROVED -> StatusTroca.APROVADO_PELO_GESTOR;
            case EFFECTIVE -> StatusTroca.EFETIVADO;
            case REJECTED -> StatusTroca.REJEITADO;
            case CANCELLED -> StatusTroca.CANCELADO;
        };
    }

    private SwapStatus toPersistenceStatus(StatusTroca status) {
        return switch (status) {
            case SOLICITADO, EM_ANALISE -> SwapStatus.PENDING;
            case APROVADO_PELO_COLEGA -> SwapStatus.COLLEAGUE_APPROVED;
            case APROVADO_PELO_GESTOR -> SwapStatus.APPROVED;
            case EFETIVADO -> SwapStatus.EFFECTIVE;
            case REJEITADO -> SwapStatus.REJECTED;
            case CANCELADO -> SwapStatus.CANCELLED;
        };
    }
}
