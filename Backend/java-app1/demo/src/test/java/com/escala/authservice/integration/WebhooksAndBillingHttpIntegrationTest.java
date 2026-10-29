package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.User;
import com.escala.authservice.repository.AiUsageRepository;
import com.escala.authservice.repository.SubscriptionRepository;
import com.stripe.Stripe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP billing, webhook Stripe e webhook do chatbot")
class WebhooksAndBillingHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AiUsageRepository aiUsageRepository;

    private static String stripeSignature(String payload, String secret) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = HexFormat.of().formatHex(
                    mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
            return "t=" + timestamp + ",v1=" + signature;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String checkoutCompletedEvent(String eventId, String subscriptionId, Company company) {
        return """
                {"id":"%s","object":"event","api_version":"%s","type":"checkout.session.completed",
                 "data":{"object":{"id":"cs_test_1","object":"checkout.session","subscription":"%s",
                 "customer":"cus_test_1","metadata":{"planType":"PROFESSIONAL","companyId":"%s"}}}}
                """.formatted(eventId, Stripe.API_VERSION, subscriptionId, company.getId());
    }

    private HttpResult postStripe(String payload, String signature) {
        return sendRaw("POST", "/api/v1/billing/webhook", null, payload, Map.of("Stripe-Signature", signature));
    }

    @Nested
    @DisplayName("POST /api/v1/billing/webhook (Stripe)")
    class StripeWebhook {

        @Test
        @DisplayName("ativa a assinatura da empresa quando a assinatura HMAC e valida")
        void activatesSubscriptionWithValidSignature() {
            Company company = persistCompany("empresa-a");
            String payload = checkoutCompletedEvent("evt_1", "sub_1", company);

            HttpResult result = postStripe(payload, stripeSignature(payload, STRIPE_WEBHOOK_SECRET));

            assertThat(result.status()).isEqualTo(200);
            assertThat(subscriptionRepository.findByStripeSubscriptionId("sub_1")).isPresent();
            assertThat(companyRepository.findById(company.getId()).orElseThrow().getPlanType())
                    .isEqualTo("PROFESSIONAL");
        }

        @Test
        @DisplayName("rejeita assinatura invalida com 400 e nao altera nada")
        void rejectsInvalidSignature() {
            Company company = persistCompany("empresa-a");
            String payload = checkoutCompletedEvent("evt_2", "sub_2", company);

            HttpResult result = postStripe(payload, stripeSignature(payload, "whsec_outro_segredo"));

            assertThat(result.status()).isEqualTo(400);
            assertThat(subscriptionRepository.count()).isZero();
            assertThat(companyRepository.findById(company.getId()).orElseThrow().getPlanType()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("rejeita payload adulterado depois de assinado")
        void rejectsTamperedPayload() {
            Company company = persistCompany("empresa-a");
            String payload = checkoutCompletedEvent("evt_3", "sub_3", company);
            String signature = stripeSignature(payload, STRIPE_WEBHOOK_SECRET);

            HttpResult result = postStripe(payload.replace("PROFESSIONAL", "CRITICAL"), signature);

            assertThat(result.status()).isEqualTo(400);
            assertThat(subscriptionRepository.count()).isZero();
        }

        @Test
        @DisplayName("reentrega do mesmo evento responde 200 sem duplicar a assinatura")
        void redeliveryDoesNotDuplicateSubscription() {
            Company company = persistCompany("empresa-a");
            String payload = checkoutCompletedEvent("evt_4", "sub_4", company);
            String signature = stripeSignature(payload, STRIPE_WEBHOOK_SECRET);

            HttpResult first = postStripe(payload, signature);
            HttpResult second = postStripe(payload, signature);

            assertThat(first.status()).isEqualTo(200);
            assertThat(second.status()).isEqualTo(200);
            assertThat(subscriptionRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks/chatbot")
    class ChatbotWebhook {

        private Map<String, Object> message() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("senderEmail", "colab@empresa-a.test");
            body.put("message", "preciso de uma troca de escala");
            return body;
        }

        @Test
        @DisplayName("nao aceita chamada sem JWT, mesmo com o segredo correto")
        void requiresAuthenticationBeforeSecret() {
            persistTenantFixture("empresa-a");

            assertThat(send("POST", "/api/v1/webhooks/chatbot", null, message(),
                    Map.of("X-Webhook-Secret", CHATBOT_SECRET)).status()).isEqualTo(401);
            assertThat(aiUsageRepository.count()).isZero();
        }

        @Test
        @DisplayName("exige o segredo configurado quando autenticado")
        void requiresSecret() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");

            assertThat(send("POST", "/api/v1/webhooks/chatbot", token, message(), null).status()).isEqualTo(403);
            assertThat(send("POST", "/api/v1/webhooks/chatbot", token, message(),
                    Map.of("X-Webhook-Secret", "errado")).status()).isEqualTo(403);
            assertThat(aiUsageRepository.count()).isZero();
        }

        @Test
        @DisplayName("responde sugestao e registra uso de IA quando autenticado e o segredo e valido")
        void acceptsValidSecret() {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            User colab = persistUser(fixture.company(), "colab@empresa-a.test", "colab-a", "USER");
            persistEmployee(fixture.company(), colab, colab.getEmail(), "Colaborador");
            String token = login("owner@empresa-a.test", "empresa-a");

            HttpResult result = send("POST", "/api/v1/webhooks/chatbot", token, message(),
                    Map.of("X-Webhook-Secret", CHATBOT_SECRET));

            assertThat(result.status()).isEqualTo(200);
            assertThat(result.json().get("status").asString()).isEqualTo("success");
            assertThat(result.json().get("action").asString()).isEqualTo("SUGGEST_REPLACEMENT");
            assertThat(aiUsageRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/billing/checkout")
    class Checkout {

        private Map<String, Object> checkout(String plan, String successUrl) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("planType", plan);
            body.put("successUrl", successUrl);
            body.put("cancelUrl", "http://localhost:3000/billing/cancelado");
            return body;
        }

        @Test
        @DisplayName("colaborador comum nao inicia checkout")
        void regularUserForbidden() {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            User regular = persistUser(fixture.company(), "user@empresa-a.test", "user-a", "USER");
            persistEmployee(fixture.company(), regular, regular.getEmail(), "Colaborador");
            String token = login("user@empresa-a.test", "empresa-a");

            HttpResult result = post("/api/v1/billing/checkout", token,
                    checkout("PROFESSIONAL", "http://localhost:3000/billing/ok"));

            assertThat(result.status()).isEqualTo(403);
        }

        @Test
        @DisplayName("rejeita URL de retorno fora das origens permitidas (open redirect)")
        void rejectsForeignRedirect() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");

            HttpResult result = post("/api/v1/billing/checkout", token,
                    checkout("PROFESSIONAL", "https://malicioso.example/steal"));

            assertThat(result.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("rejeita plano desconhecido sem chamar o gateway")
        void rejectsUnknownPlan() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");

            HttpResult result = post("/api/v1/billing/checkout", token,
                    checkout("PLANO_GRATIS_PARA_SEMPRE", "http://localhost:3000/billing/ok"));

            assertThat(result.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
        }

        @Test
        @DisplayName("assinatura de uma empresa nao e visivel para outra")
        void subscriptionIsTenantScoped() {
            persistTenantFixture("empresa-a");
            persistTenantFixture("empresa-b");
            String tokenB = login("owner@empresa-b.test", "empresa-b");

            assertThat(get("/api/v1/billing/subscription", tokenB).status()).isEqualTo(404);
        }
    }
}
