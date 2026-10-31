package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Subscription;
import com.escala.authservice.entity.SubscriptionStatus;
import com.escala.authservice.repository.SubscriptionRepository;
import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP /api/v1/billing/webhook: idempotencia, ordem e erros permanentes")
class StripeWebhookIdempotencyHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Company company;

    @BeforeEach
    void seed() {
        company = persistCompany("empresa-a");
    }

    private static String signature(String payload) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(STRIPE_WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String hex = HexFormat.of().formatHex(
                    mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
            return "t=" + timestamp + ",v1=" + hex;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private HttpResult deliver(String payload) {
        return sendRaw("POST", "/api/v1/billing/webhook", null, payload, Map.of("Stripe-Signature", signature(payload)));
    }

    private String checkoutCompleted(String eventId, long created, String subscriptionId, String planType) {
        String metadata = planType == null
                ? "{\"companyId\":\"%s\"}".formatted(company.getId())
                : "{\"planType\":\"%s\",\"companyId\":\"%s\"}".formatted(planType, company.getId());
        return """
                {"id":"%s","object":"event","api_version":"%s","created":%d,"type":"checkout.session.completed",
                 "data":{"object":{"id":"cs_%s","object":"checkout.session","subscription":"%s",
                 "customer":"cus_test_1","metadata":%s}}}
                """.formatted(eventId, Stripe.API_VERSION, created, eventId, subscriptionId, metadata);
    }

    private String subscriptionEvent(String eventId, long created, String type, String subscriptionId, String status) {
        return """
                {"id":"%s","object":"event","api_version":"%s","created":%d,"type":"%s",
                 "data":{"object":{"id":"%s","object":"subscription","customer":"cus_test_1","status":"%s",
                 "metadata":{"planType":"PROFESSIONAL","companyId":"%s"}}}}
                """.formatted(eventId, Stripe.API_VERSION, created, type, subscriptionId, status, company.getId());
    }

    private List<HttpResult> deliverInParallel(String payload, int count) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(count);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<HttpResult>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Callable<HttpResult> task = () -> {
                    start.await();
                    return deliver(payload);
                };
                futures.add(pool.submit(task));
            }
            start.countDown();
            List<HttpResult> results = new ArrayList<>();
            for (Future<HttpResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    private long processedEvents(String outcome) {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from stripe_processed_events where outcome = ?", Long.class, outcome);
        return count == null ? 0 : count;
    }

    @Nested
    @DisplayName("entrega duplicada")
    class DuplicateDelivery {

        @Test
        @DisplayName("o mesmo evento entregue em paralelo responde 200 para todos e processa uma vez")
        void parallelRedeliveryProcessesOnce() throws Exception {
            String payload = checkoutCompleted("evt_dup", 1_000, "sub_dup", "PROFESSIONAL");

            List<HttpResult> results = deliverInParallel(payload, 8);

            assertThat(results).extracting(HttpResult::status).containsOnly(200);
            assertThat(subscriptionRepository.count()).isEqualTo(1);
            assertThat(processedEvents("PROCESSED")).isEqualTo(1);
        }

        @Test
        @DisplayName("reentrega sequencial nao reaplica o evento")
        void sequentialRedeliveryIsIgnored() {
            String payload = checkoutCompleted("evt_seq", 1_000, "sub_seq", "PROFESSIONAL");

            assertThat(deliver(payload).status()).isEqualTo(200);
            Subscription stored = subscriptionRepository.findByStripeSubscriptionId("sub_seq").orElseThrow();
            stored.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.saveAndFlush(stored);

            assertThat(deliver(payload).status()).isEqualTo(200);

            assertThat(subscriptionRepository.findByStripeSubscriptionId("sub_seq").orElseThrow().getStatus())
                    .as("o segundo envio nao pode sobrescrever o estado")
                    .isEqualTo(SubscriptionStatus.PAST_DUE);
        }
    }

    @Nested
    @DisplayName("eventos fora de ordem")
    class OutOfOrder {

        @Test
        @DisplayName("um updated antigo nao reativa uma assinatura ja cancelada")
        void staleUpdateDoesNotReviveCanceledSubscription() {
            assertThat(deliver(checkoutCompleted("evt_1", 1_000, "sub_ooo", "PROFESSIONAL")).status()).isEqualTo(200);
            assertThat(deliver(subscriptionEvent("evt_3", 3_000, "customer.subscription.deleted",
                    "sub_ooo", "canceled")).status()).isEqualTo(200);

            HttpResult stale = deliver(subscriptionEvent("evt_2", 2_000, "customer.subscription.updated",
                    "sub_ooo", "active"));

            assertThat(stale.status()).isEqualTo(200);
            assertThat(subscriptionRepository.findByStripeSubscriptionId("sub_ooo").orElseThrow().getStatus())
                    .isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(companyRepository.findById(company.getId()).orElseThrow().getPlanType()).isEqualTo("FREE");
        }
    }

    @Nested
    @DisplayName("erro permanente no payload")
    class PermanentFailure {

        @Test
        @DisplayName("metadata obrigatoria ausente responde 200, nao altera nada e registra a falha")
        void missingMetadataIsRecordedNotRetried() {
            HttpResult result = deliver(checkoutCompleted("evt_bad", 1_000, "sub_bad", null));

            assertThat(result.status()).isEqualTo(200);
            assertThat(subscriptionRepository.count()).isZero();
            assertThat(processedEvents("FAILED")).isEqualTo(1);
        }
    }
}
