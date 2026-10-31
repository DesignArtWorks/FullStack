package com.escala.authservice.integration;

import com.escala.authservice.repository.MarketingLeadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP POST /api/v1/leads: captura publica via BFF")
class PublicLeadHttpIntegrationTest extends AbstractHttpIntegrationTest {

    private static final Map<String, String> BFF_HEADERS = Map.of(
            "X-Requested-With", "escala-next-bff",
            "X-Escala-Bff-Secret", BFF_SECRET
    );

    @Autowired
    private MarketingLeadRepository leadRepository;

    private Map<String, Object> lead(String email) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Maria Souza");
        body.put("email", email);
        body.put("companyName", "Padaria Bom Pao");
        body.put("employeeRange", "1-10");
        body.put("companySegment", "varejo");
        body.put("marketingConsentGranted", true);
        body.put("consentVersion", "2026-01");
        body.put("utmSource", "google");
        body.put("utmCampaign", "escala-mensal");
        body.put("landingPageSlug", "pequeno-varejo");
        return body;
    }

    @Test
    @DisplayName("persiste o lead com atribuicao quando a chamada vem do BFF")
    void capturesLeadFromBff() {
        HttpResult result = send("POST", "/api/v1/leads", null, lead("maria@padaria.test"), BFF_HEADERS);

        assertThat(result.status()).isEqualTo(200);
        assertThat(leadRepository.count()).isEqualTo(1);
        assertThat(leadRepository.findAll().getFirst().getEmail()).isEqualTo("maria@padaria.test");
    }

    @Test
    @DisplayName("bloqueia chamada direta sem os cabecalhos do BFF")
    void blocksDirectCalls() {
        HttpResult result = send("POST", "/api/v1/leads", null, lead("direto@padaria.test"), null);

        assertThat(result.status()).isEqualTo(403);
        assertThat(leadRepository.count()).isZero();
    }

    @Test
    @DisplayName("bloqueia segredo do BFF incorreto")
    void blocksWrongBffSecret() {
        HttpResult result = send("POST", "/api/v1/leads", null, lead("errado@padaria.test"),
                Map.of("X-Requested-With", "escala-next-bff", "X-Escala-Bff-Secret", "errado"));

        assertThat(result.status()).isEqualTo(403);
        assertThat(leadRepository.count()).isZero();
    }

    @Test
    @DisplayName("aplica limite de taxa depois de repetidas tentativas (429)")
    void rateLimitsRepeatedSubmissions() {
        int tooMany = 0;
        for (int attempt = 0; attempt < 20; attempt++) {
            HttpResult result = send("POST", "/api/v1/leads", null, lead("spam@padaria.test"), BFF_HEADERS);
            if (result.status() == 429) {
                tooMany++;
            }
        }

        assertThat(tooMany).isGreaterThan(0);
    }
}
