package com.escala.authservice.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rede de seguranca: todo endpoint que altera estado precisa estar neste inventario.
 * Ao adicionar um endpoint novo, o build falha ate alguem registra-lo aqui, o que obriga
 * a decidir e testar quem pode chama-lo (papel, tenant) antes do merge.
 */
@DisplayName("Inventario de endpoints mutaveis")
class MutatingEndpointInventoryIntegrationTest extends AbstractHttpIntegrationTest {

    private static final Set<RequestMethod> MUTATING = Set.of(
            RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE);

    private static final Set<String> EXPECTED = Set.of(
            "DELETE /api/v1/companies/{id}",
            "DELETE /api/v1/employees/{id}",
            "DELETE /api/v1/escala/{id}",
            "DELETE /api/v1/operational-capacities/{id}",
            "DELETE /api/v1/organization/projects/{id}",
            "DELETE /api/v1/organization/sectors/{id}",
            "DELETE /api/v1/rebac/management-edges/{id}",
            "DELETE /api/v1/rebac/manager-assignments/{id}",
            "DELETE /api/v1/team/invitations/{id}",
            "DELETE /api/v1/users/{id}/roles",
            "DELETE /api/v1/work-posts/{id}",
            "PATCH /api/v1/learning-progress/{id}/complete",
            "PATCH /api/v1/messages/{id}/decision",
            "PATCH /api/v1/schedules/swap-requests/{id}/colleague-approval",
            "PATCH /api/v1/schedules/swap-requests/{id}/decision",
            "PATCH /api/v1/scheduling/cycles/{id}/assignments",
            "PATCH /api/v1/users/me",
            "PATCH /api/v1/users/me/password",
            "PATCH /api/v1/users/{id}/theme",
            "POST /api/v1/ai/analyze-risk",
            "POST /api/v1/ai/explain-conflict",
            "POST /api/v1/ai/suggest-replacement",
            "POST /api/v1/auth/authenticate",
            "POST /api/v1/auth/complete-registration",
            "POST /api/v1/auth/forgot-password",
            "POST /api/v1/auth/google",
            "POST /api/v1/auth/register",
            "POST /api/v1/auth/reset-password",
            "POST /api/v1/billing/cancel",
            "POST /api/v1/billing/checkout",
            "POST /api/v1/billing/webhook",
            "POST /api/v1/check-in",
            "POST /api/v1/companies",
            "POST /api/v1/employees",
            "POST /api/v1/escala",
            "POST /api/v1/leads",
            "POST /api/v1/learning-progress",
            "POST /api/v1/messages",
            "POST /api/v1/operational-capacities",
            "POST /api/v1/organization/projects",
            "POST /api/v1/organization/sectors",
            "POST /api/v1/public/contact",
            "POST /api/v1/rebac/management-closure/recalculate",
            "POST /api/v1/rebac/management-edges",
            "POST /api/v1/rebac/manager-assignments",
            "POST /api/v1/schedules/generate",
            "POST /api/v1/schedules/swap-requests",
            "POST /api/v1/scheduling/cycles",
            "POST /api/v1/scheduling/cycles/{id}/alerts/{alertId}/acknowledge",
            "POST /api/v1/scheduling/cycles/{id}/archive",
            "POST /api/v1/scheduling/cycles/{id}/publish",
            "POST /api/v1/scheduling/cycles/{id}/rectify",
            "POST /api/v1/scheduling/cycles/{id}/validate",
            "POST /api/v1/scheduling/holidays",
            "POST /api/v1/team/invitations",
            "POST /api/v1/users/{id}/roles",
            "POST /api/v1/webhooks/chatbot",
            "POST /api/v1/work-posts",
            "PUT /api/v1/companies/{id}",
            "PUT /api/v1/employees/{id}",
            "PUT /api/v1/escala/{id}",
            "PUT /api/v1/operational-capacities/{id}",
            "PUT /api/v1/organization/projects/{id}",
            "PUT /api/v1/organization/sectors/{id}"
    );

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    @DisplayName("nenhum endpoint mutavel novo existe sem ser registrado e revisado")
    void everyMutatingEndpointIsRegistered() {
        Set<String> actual = new TreeSet<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                if (MUTATING.contains(method) && info.getPathPatternsCondition() != null) {
                    info.getPathPatternsCondition().getPatternValues()
                            .forEach(path -> actual.add(method + " " + path));
                }
            }
        }

        assertThat(actual)
                .as("Registre o endpoint novo em EXPECTED depois de definir e testar papel e tenant")
                .containsExactlyInAnyOrderElementsOf(EXPECTED);
    }
}
