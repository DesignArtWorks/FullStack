package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP POST /api/v1/check-in: ponto web com geolocalizacao")
class CheckInHttpIntegrationTest extends AbstractHttpIntegrationTest {

    private static final double OFFICE_LAT = -23.55052;
    private static final double OFFICE_LON = -46.633308;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    private String token;

    @BeforeEach
    void givenCompanyWithGeofence() {
        TenantFixture fixture = persistTenantFixture("empresa-a");
        Company company = fixture.company();
        company.setLatitude(OFFICE_LAT);
        company.setLongitude(OFFICE_LON);
        company.setAllowedRadius(200);
        company.setPlanType("PROFESSIONAL");
        companyRepository.saveAndFlush(company);
        token = login("owner@empresa-a.test", "empresa-a");
    }

    private Map<String, Object> checkIn(double lat, double lon) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("latitude", lat);
        body.put("longitude", lon);
        body.put("deviceFingerprint", "device-test-1");
        return body;
    }

    @Test
    @DisplayName("registra ENTRY dentro do raio e alterna para EXIT no segundo registro")
    void alternatesEntryAndExit() {
        assertThat(post("/api/v1/check-in", token, checkIn(OFFICE_LAT, OFFICE_LON)).status()).isEqualTo(200);
        assertThat(post("/api/v1/check-in", token, checkIn(OFFICE_LAT, OFFICE_LON)).status()).isEqualTo(200);

        assertThat(timeRecordRepository.findAll()).extracting(r -> r.getType().name())
                .containsExactlyInAnyOrder("ENTRY", "EXIT");
    }

    @Test
    @DisplayName("recusa ponto fora do raio permitido e nao persiste registro")
    void rejectsOutsideRadius() {
        HttpResult result = post("/api/v1/check-in", token, checkIn(OFFICE_LAT + 0.05, OFFICE_LON));

        assertThat(result.status()).isEqualTo(403);
        assertThat(timeRecordRepository.count()).isZero();
    }

    @Test
    @DisplayName("exige coordenadas e device fingerprint")
    void requiresCoordinatesAndDevice() {
        Map<String, Object> noCoordinates = new LinkedHashMap<>();
        noCoordinates.put("deviceFingerprint", "device-test-1");
        Map<String, Object> noDevice = new LinkedHashMap<>();
        noDevice.put("latitude", OFFICE_LAT);
        noDevice.put("longitude", OFFICE_LON);

        assertThat(post("/api/v1/check-in", token, noCoordinates).status()).isEqualTo(400);
        assertThat(post("/api/v1/check-in", token, noDevice).status()).isEqualTo(400);
        assertThat(timeRecordRepository.count()).isZero();
    }

    /**
     * Sinal para a Fase 3: o tipo do proximo registro e calculado lendo os registros do dia e
     * gravando depois, sem trava. Requisicoes simultaneas do mesmo usuario podem gerar dois ENTRY.
     * Com N requisicoes simultaneas, a alternancia correta termina com |ENTRY - EXIT| <= 1.
     */
    @Test
    @DisplayName("requisicoes simultaneas do mesmo usuario mantem a alternancia ENTRY/EXIT consistente")
    void concurrentCheckInsKeepAlternation() throws Exception {
        int requests = 8;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        try {
            List<Callable<HttpResult>> calls = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                calls.add(() -> post("/api/v1/check-in", token, checkIn(OFFICE_LAT, OFFICE_LON)));
            }
            List<HttpResult> results = new ArrayList<>();
            for (Future<HttpResult> future : pool.invokeAll(calls)) {
                results.add(future.get());
            }

            assertThat(results).allSatisfy(r -> assertThat(r.status()).isIn(200, 409, 429));
            long entries = timeRecordRepository.findAll().stream()
                    .filter(r -> r.getType().name().equals("ENTRY")).count();
            long exits = timeRecordRepository.count() - entries;
            assertThat(Math.abs(entries - exits)).isLessThanOrEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
