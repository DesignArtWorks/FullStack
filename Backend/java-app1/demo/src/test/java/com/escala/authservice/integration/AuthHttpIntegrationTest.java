package com.escala.authservice.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP /api/v1/auth e protecao de rotas privadas")
class AuthHttpIntegrationTest extends AbstractHttpIntegrationTest {

    private Map<String, Object> credentials(String email, String password, String slug) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("companySlug", slug);
        return body;
    }

    @Nested
    @DisplayName("POST /authenticate")
    class Authenticate {

        @Test
        @DisplayName("devolve JWT e dados do usuario quando as credenciais sao validas")
        void returnsTokenForValidCredentials() {
            persistTenantFixture("empresa-a");

            HttpResult result = post("/api/v1/auth/authenticate", null,
                    credentials("owner@empresa-a.test", TEST_PASSWORD, "empresa-a"));

            assertThat(result.status()).isEqualTo(200);
            assertThat(result.json().get("token").asString()).isNotBlank();
            assertThat(result.json().get("user").get("email").asString()).isEqualTo("owner@empresa-a.test");
            assertThat(result.json().get("user").get("companySlug").asString()).isEqualTo("empresa-a");
            assertThat(result.body()).doesNotContain("password");
        }

        @Test
        @DisplayName("rejeita senha incorreta sem emitir token")
        void rejectsWrongPassword() {
            persistTenantFixture("empresa-a");

            HttpResult result = post("/api/v1/auth/authenticate", null,
                    credentials("owner@empresa-a.test", "senha-errada-123", "empresa-a"));

            assertThat(result.status()).isIn(400, 401, 403);
            assertThat(result.body()).doesNotContain("\"token\"");
        }

        @Test
        @DisplayName("nao autentica usuario de uma empresa usando o slug de outra")
        void rejectsCredentialsOfAnotherTenant() {
            persistTenantFixture("empresa-a");
            persistTenantFixture("empresa-b");

            HttpResult result = post("/api/v1/auth/authenticate", null,
                    credentials("owner@empresa-a.test", TEST_PASSWORD, "empresa-b"));

            assertThat(result.status()).isIn(400, 401, 403, 404);
            assertThat(result.body()).doesNotContain("\"token\"");
        }
    }

    @Nested
    @DisplayName("POST /register")
    class Register {

        @Test
        @DisplayName("cria empresa e OWNER no fluxo self-service")
        void registersOwnerWithNewCompany() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", "novo-owner");
            body.put("email", "novo@empresa-nova.test");
            body.put("password", TEST_PASSWORD);
            body.put("companyName", "Empresa Nova");

            HttpResult result = post("/api/v1/auth/register", null, body);

            assertThat(result.status()).isEqualTo(200);
            assertThat(result.json().get("token").asString()).isNotBlank();
            assertThat(result.json().get("user").get("roles").toString()).contains("OWNER");
            assertThat(companyRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("rejeita senha com menos de 8 caracteres")
        void rejectsShortPassword() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", "curto");
            body.put("email", "curto@empresa-nova.test");
            body.put("password", "1234567");
            body.put("companyName", "Empresa Curta");

            HttpResult result = post("/api/v1/auth/register", null, body);

            assertThat(result.status()).isEqualTo(400);
            assertThat(companyRepository.count()).isZero();
        }

        @Test
        @DisplayName("nao permite entrar em empresa existente sem convite ativo")
        void rejectsRegistrationIntoExistingCompanyWithoutInvitation() {
            persistTenantFixture("empresa-a");
            long usersBefore = userRepository.count();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", "intruso");
            body.put("email", "intruso@externo.test");
            body.put("password", TEST_PASSWORD);
            body.put("companySlug", "empresa-a");

            HttpResult result = post("/api/v1/auth/register", null, body);

            assertThat(result.status()).isGreaterThanOrEqualTo(400);
            assertThat(userRepository.count()).isEqualTo(usersBefore);
        }
    }

    @Nested
    @DisplayName("rotas privadas")
    class PrivateRoutes {

        @Test
        @DisplayName("respondem 401 sem token")
        void requireAuthentication() {
            assertThat(get("/api/v1/schedules/swap-requests", null).status()).isEqualTo(401);
            assertThat(get("/api/v1/scheduling/cycles", null).status()).isEqualTo(401);
            assertThat(get("/api/v1/billing/subscription", null).status()).isEqualTo(401);
            assertThat(post("/api/v1/check-in", null, Map.of()).status()).isEqualTo(401);
        }

        @Test
        @DisplayName("respondem 401 com token adulterado")
        void rejectTamperedToken() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");
            String tampered = token.substring(0, token.length() - 4) + "AAAA";

            assertThat(get("/api/v1/scheduling/cycles", tampered).status()).isEqualTo(401);
        }

        @Test
        @DisplayName("aceitam token valido")
        void acceptValidToken() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");

            assertThat(get("/api/v1/scheduling/cycles", token).status()).isEqualTo(200);
        }

        @Test
        @DisplayName("mantem o health publico e as metricas do actuator protegidas")
        void actuatorExposure() {
            assertThat(get("/actuator/health", null).status()).isEqualTo(200);
            assertThat(get("/actuator/metrics", null).status()).isIn(401, 403);
            assertThat(get("/actuator/prometheus", null).status()).isIn(401, 403);
        }
    }
}
