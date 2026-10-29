package com.escala.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Registro de eventos do Stripe ja tratados. O insert com ON CONFLICT e atomico: entregas
 * simultaneas do mesmo evento esperam o commit da primeira e depois nao inserem nada.
 */
@Component
@RequiredArgsConstructor
public class StripeEventStore {
    private static final int DETAIL_LIMIT = 500;

    private final JdbcTemplate jdbcTemplate;

    /** Reserva o evento na transacao corrente. Falso quando ele ja foi tratado ou registrado como falha. */
    public boolean claim(String eventId, String eventType) {
        return insert(eventId, eventType, "PROCESSED", null);
    }

    public void recordFailure(String eventId, String eventType, String detail) {
        insert(eventId, eventType, "FAILED", detail);
    }

    private boolean insert(String eventId, String eventType, String outcome, String detail) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("id do evento obrigatorio");
        }
        String safeDetail = detail == null || detail.length() <= DETAIL_LIMIT
                ? detail
                : detail.substring(0, DETAIL_LIMIT);
        return jdbcTemplate.update(
                "insert into stripe_processed_events (event_id, event_type, outcome, detail) values (?, ?, ?, ?) "
                        + "on conflict (event_id) do nothing",
                eventId, eventType == null ? "unknown" : eventType, outcome, safeDetail) == 1;
    }
}
