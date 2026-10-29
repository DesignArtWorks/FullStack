package com.escala.authservice.service;

import com.escala.authservice.core.billing.usecase.BillingService;
import com.escala.authservice.entity.SubscriptionStatus;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Processa eventos do Stripe de forma idempotente: cada evento e reservado e aplicado na mesma
 * transacao. Falha transitoria desfaz a reserva (o Stripe reenvia); payload invalido e registrado
 * como FAILED e respondido com sucesso para o Stripe nao reenviar um evento que nunca vai passar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {
    private final BillingService billingService;
    private final StripeEventStore eventStore;
    private final TransactionTemplate transactionTemplate;

    public String process(Event event, StripeObject stripeObject) {
        try {
            Boolean applied = transactionTemplate.execute(status -> {
                if (!eventStore.claim(event.getId(), event.getType())) {
                    return false;
                }
                dispatch(event, stripeObject);
                return true;
            });
            return Boolean.TRUE.equals(applied) ? "Evento processado" : "Evento ja processado";
        } catch (IllegalArgumentException invalidEvent) {
            log.error("Evento do Stripe invalido, registrado como FAILED: id={} tipo={} motivo={}",
                    event.getId(), event.getType(), invalidEvent.getMessage());
            transactionTemplate.executeWithoutResult(status ->
                    eventStore.recordFailure(event.getId(), event.getType(), invalidEvent.getMessage()));
            return "Evento invalido registrado";
        }
    }

    private void dispatch(Event event, StripeObject stripeObject) {
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted((Session) stripeObject, event.getCreated());
                break;
            case "customer.subscription.updated":
            case "customer.subscription.deleted":
                handleSubscriptionUpdated((Subscription) stripeObject, event.getCreated());
                break;
            default:
                log.info("Evento do Stripe nao tratado: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Session session, Long eventCreated) {
        String stripeSubscriptionId = requireNonBlank(session.getSubscription(), "subscriptionId");
        String stripeCustomerId = requireNonBlank(session.getCustomer(), "customerId");
        String planType = requireMetadata(session.getMetadata(), "planType");
        UUID companyId = requireUuidMetadata(session.getMetadata(), "companyId");

        billingService.updateSubscriptionStatus(
                stripeSubscriptionId,
                stripeCustomerId,
                SubscriptionStatus.ACTIVE,
                planType,
                companyId,
                eventCreated
        );
    }

    private void handleSubscriptionUpdated(Subscription stripeSubscription, Long eventCreated) {
        String stripeSubscriptionId = requireNonBlank(stripeSubscription.getId(), "subscriptionId");
        String stripeCustomerId = requireNonBlank(stripeSubscription.getCustomer(), "customerId");
        SubscriptionStatus status = mapStripeStatus(stripeSubscription.getStatus());
        String planType = readMetadata(stripeSubscription.getMetadata(), "planType");
        UUID companyId = readUuidMetadata(stripeSubscription.getMetadata(), "companyId");

        billingService.updateSubscriptionStatus(
                stripeSubscriptionId,
                stripeCustomerId,
                status,
                planType,
                companyId,
                eventCreated
        );
    }

    private String requireMetadata(Map<String, String> metadata, String key) {
        String value = readMetadata(metadata, key);
        if (value == null) {
            throw new IllegalArgumentException("Metadata obrigatoria ausente: " + key);
        }
        return value;
    }

    private UUID requireUuidMetadata(Map<String, String> metadata, String key) {
        UUID value = readUuidMetadata(metadata, key);
        if (value == null) {
            throw new IllegalArgumentException("Metadata UUID obrigatoria ausente: " + key);
        }
        return value;
    }

    private String readMetadata(Map<String, String> metadata, String key) {
        if (metadata == null) {
            return null;
        }

        String value = metadata.get(key);
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private UUID readUuidMetadata(Map<String, String> metadata, String key) {
        String value = readMetadata(metadata, key);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " obrigatorio");
        }
        return value;
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            case "incomplete_expired" -> SubscriptionStatus.INCOMPLETE_EXPIRED;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "unpaid" -> SubscriptionStatus.UNPAID;
            default -> SubscriptionStatus.CANCELED;
        };
    }
}
