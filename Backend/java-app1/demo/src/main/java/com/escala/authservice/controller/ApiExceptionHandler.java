package com.escala.authservice.controller;

import com.escala.authservice.observability.CorrelationIdFilter;
import com.escala.authservice.scheduling.domain.exception.TrocaInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Acesso negado.", ex, request, false);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "OPTIMISTIC_LOCK_CONFLICT",
                "O registro foi alterado por outro usuario. Atualize a tela e tente novamente.", ex, request, false);
    }

    @ExceptionHandler(TrocaInvalidaException.class)
    public ResponseEntity<ApiError> handleTrocaInvalida(TrocaInvalidaException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_SWAP", safeMessage(ex), ex, request, false);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dados informados sao invalidos.", ex, request, false);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", safeMessage(ex), ex, request, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", safeMessage(ex), ex, request, false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Nao foi possivel concluir a operacao.", ex, request, true);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, Exception ex,
                                            HttpServletRequest request, boolean unexpected) {
        String correlationId = CorrelationIdFilter.from(request);
        String errorId = "ERR-" + UUID.randomUUID();
        if (unexpected) {
            LOGGER.error("event=api_error status={} errorCode={} errorId={} route={} outcome=failure",
                    status.value(), code, errorId, request.getRequestURI(), ex);
        } else {
            LOGGER.warn("event=api_error status={} errorCode={} errorId={} route={} outcome=rejected type={}",
                    status.value(), code, errorId, request.getRequestURI(), ex.getClass().getSimpleName());
        }
        return ResponseEntity.status(status)
                .header(CorrelationIdFilter.HEADER, correlationId)
                .body(new ApiError(code, message, status.value(), errorId, correlationId));
    }

    private String safeMessage(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "A operacao informada e invalida."
                : ex.getMessage();
    }
}
