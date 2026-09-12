package com.escala.authservice.controller;

import com.escala.authservice.observability.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void unexpectedErrorsReturnSafeCorrelatedContract() {
        MockHttpServletRequest request = request("corr-97");

        var response = handler.handleUnexpected(new RuntimeException("jdbc:postgresql://secret internal SQL"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("jdbc", "SQL", "secret");
        assertThat(response.getBody().correlationId()).isEqualTo("corr-97");
        assertThat(response.getBody().errorId()).startsWith("ERR-");
    }

    @Test
    void accessDeniedDoesNotExposeInternalMessage() {
        var response = handler.handleAccessDenied(
                new org.springframework.security.access.AccessDeniedException("tenant 42 does not own resource 9"),
                request("corr-denied"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Acesso negado.");
        assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
    }

    private MockHttpServletRequest request(String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/private");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE, correlationId);
        return request;
    }
}
