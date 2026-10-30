package com.escala.authservice.integration;

import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.PasswordResetToken;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.User;
import com.escala.authservice.entity.WorkShift;
import com.escala.authservice.repository.MarketingLeadRepository;
import com.escala.authservice.repository.PasswordResetTokenRepository;
import com.escala.authservice.repository.SectorRepository;
import com.escala.authservice.repository.ShiftSwapRequestRepository;
import com.escala.authservice.repository.WorkShiftRepository;
import com.escala.authservice.service.SensitiveTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP: repeticoes e duplo clique nao duplicam efeitos")
class RepeatedRequestHttpIntegrationTest extends AbstractHttpIntegrationTest {

    private static final int PARALLEL = 8;

    @Autowired
    private MarketingLeadRepository leadRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private SensitiveTokenService sensitiveTokenService;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private ShiftSwapRequestRepository swapRepository;

    private List<HttpResult> inParallel(Supplier<HttpResult> call) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<HttpResult>> futures = new ArrayList<>();
            for (int i = 0; i < PARALLEL; i++) {
                Callable<HttpResult> task = () -> {
                    start.await();
                    return call.get();
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

    private static long count(List<HttpResult> results, int status) {
        return results.stream().filter(r -> r.status() == status).count();
    }

    @Nested
    @DisplayName("captura de lead")
    class LeadCapture {

        @Test
        @DisplayName("envios simultaneos do mesmo e-mail geram um unico lead")
        void parallelSubmissionsKeepOneLead() throws Exception {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("name", "Maria Souza");
            body.put("email", "maria@padaria.test");
            body.put("companyName", "Padaria Bom Pao");
            body.put("employeeRange", "1-10");
            body.put("companySegment", "varejo");
            body.put("marketingConsentGranted", true);
            body.put("consentVersion", "2026-01");
            Map<String, String> headers = Map.of(
                    "X-Requested-With", "escala-next-bff",
                    "X-Escala-Bff-Secret", BFF_SECRET);

            List<HttpResult> results = inParallel(() -> send("POST", "/api/v1/leads", null, body, headers));

            assertThat(results).extracting(HttpResult::status).containsOnly(200);
            assertThat(leadRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("redefinicao de senha com token de uso unico")
    class PasswordReset {

        @Test
        @DisplayName("o mesmo codigo usado em paralelo e aceito uma unica vez")
        void tokenIsConsumedOnce() throws Exception {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            String code = "codigo-de-recuperacao-123";
            resetTokenRepository.saveAndFlush(PasswordResetToken.builder()
                    .user(fixture.owner())
                    .tokenHash(sensitiveTokenService.sha256Hex(code))
                    .expiresAt(OffsetDateTime.now().plusHours(1))
                    .build());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("code", code);
            body.put("password", "NovaSenha@123");
            body.put("passwordConfirmation", "NovaSenha@123");

            List<HttpResult> results = inParallel(() -> send("POST", "/api/v1/auth/reset-password", null, body, null));

            assertThat(results).allSatisfy(r -> assertThat(r.status()).isNotEqualTo(500));
            assertThat(count(results, 200)).as("redefinicoes aceitas").isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("solicitacao de troca de escala")
    class SwapRequest {

        private WorkShift shiftOfRequester(Employee requester) {
            return workShiftRepository.saveAndFlush(WorkShift.builder()
                    .employee(requester)
                    .shiftDate(LocalDate.now().plusDays(10))
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
                    .status(ShiftStatus.SCHEDULED)
                    .build());
        }

        private Employee requesterIn(TenantFixture fixture) {
            Sector sector = sectorRepository.saveAndFlush(
                    Sector.builder().name("Atendimento").company(fixture.company()).build());
            User user = persistUser(fixture.company(), "solicitante@empresa-a.test", "solicitante-a", "USER");
            Employee employee = persistEmployee(fixture.company(), user, user.getEmail(), "Solicitante");
            employee.setSector(sector);
            return employeeRepository.saveAndFlush(employee);
        }

        private Map<String, Object> swap(WorkShift shift) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("originalShiftId", shift.getId().toString());
            body.put("compensationDate", LocalDate.now().plusDays(12).toString());
            return body;
        }

        @Test
        @DisplayName("pedidos simultaneos para a mesma escala criam uma unica solicitacao aberta")
        void parallelRequestsKeepOneOpenSwap() throws Exception {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            WorkShift shift = shiftOfRequester(requesterIn(fixture));
            String token = login("solicitante@empresa-a.test", "empresa-a");

            List<HttpResult> results = inParallel(() -> post("/api/v1/schedules/swap-requests", token, swap(shift)));

            assertThat(results).allSatisfy(r -> assertThat(r.status()).isNotEqualTo(500));
            assertThat(count(results, 200)).as("solicitacoes aceitas").isEqualTo(1);
            assertThat(swapRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("repetir o pedido depois do primeiro e recusado com conflito")
        void sequentialRepeatIsRejected() {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            WorkShift shift = shiftOfRequester(requesterIn(fixture));
            String token = login("solicitante@empresa-a.test", "empresa-a");

            assertThat(post("/api/v1/schedules/swap-requests", token, swap(shift)).status()).isEqualTo(200);
            HttpResult repeated = post("/api/v1/schedules/swap-requests", token, swap(shift));

            assertThat(repeated.status()).isEqualTo(409);
            assertThat(swapRepository.count()).isEqualTo(1);
        }
    }
}
