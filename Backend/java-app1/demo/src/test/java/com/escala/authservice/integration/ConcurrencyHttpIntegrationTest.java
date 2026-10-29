package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.User;
import com.escala.authservice.entity.WorkShift;
import com.escala.authservice.repository.SectorRepository;
import com.escala.authservice.repository.ShiftSwapRequestRepository;
import com.escala.authservice.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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

@DisplayName("HTTP concorrente: requisicoes simultaneas nao quebram invariantes de negocio")
class ConcurrencyHttpIntegrationTest extends AbstractHttpIntegrationTest {

    private static final int PARALLEL = 8;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private ShiftSwapRequestRepository swapRepository;

    private Company company;
    private Sector sector;
    private String managerToken;

    @BeforeEach
    void seed() {
        TenantFixture fixture = persistTenantFixture("empresa-a");
        company = fixture.company();
        sector = sectorRepository.saveAndFlush(Sector.builder().name("Atendimento").company(company).build());
        managerToken = login("owner@empresa-a.test", "empresa-a");
    }

    private Employee employeeInSector(String key) {
        User user = persistUser(company, key + "@empresa-a.test", key + "-a", "USER");
        Employee employee = persistEmployee(company, user, user.getEmail(), key);
        employee.setSector(sector);
        return employeeRepository.saveAndFlush(employee);
    }

    /** Dispara as chamadas ao mesmo tempo (barreira de largada) e devolve todos os resultados. */
    private List<HttpResult> inParallel(int count, Supplier<HttpResult> call) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(count);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Callable<HttpResult>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                tasks.add(() -> {
                    start.await();
                    return call.get();
                });
            }
            List<Future<HttpResult>> futures = new ArrayList<>();
            for (Callable<HttpResult> task : tasks) {
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
    @DisplayName("escala do mesmo funcionario no mesmo dia")
    class DuplicateShift {

        @Test
        @DisplayName("criacoes simultaneas geram uma unica escala e as demais sao rejeitadas")
        void parallelCreationsKeepASingleShift() throws Exception {
            Employee employee = employeeInSector("colab");
            LocalDate date = LocalDate.now().plusDays(21).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("employeeId", employee.getId().toString());
            body.put("sectorId", sector.getId().toString());
            body.put("dates", List.of(date.toString()));
            body.put("startTime", "08:00:00");
            body.put("endTime", "17:00:00");
            body.put("workMode", "PRESENCIAL");

            List<HttpResult> results = inParallel(PARALLEL,
                    () -> post("/api/v1/escala", managerToken, body));

            assertThat(results).allSatisfy(r -> assertThat(r.status()).isNotEqualTo(500));
            assertThat(count(results, 200)).as("criacoes aceitas").isEqualTo(1);
            long active = workShiftRepository.findAll().stream()
                    .filter(s -> s.getEmployee().getId().equals(employee.getId()))
                    .filter(s -> s.getShiftDate().equals(date))
                    .filter(s -> s.getStatus() != ShiftStatus.CANCELLED)
                    .count();
            assertThat(active).as("escalas ativas do funcionario no dia").isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("decisao do gestor sobre a troca")
    class SwapDecision {

        @Test
        @DisplayName("decisoes simultaneas aprovam uma vez so e agendam uma unica compensacao")
        void parallelApprovalsApplyOnce() throws Exception {
            Employee requester = employeeInSector("solicitante");
            LocalDate shiftDate = LocalDate.now().plusDays(10);
            LocalDate compensationDate = LocalDate.now().plusDays(12);
            WorkShift original = workShiftRepository.saveAndFlush(WorkShift.builder()
                    .employee(requester)
                    .shiftDate(shiftDate)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
                    .status(ShiftStatus.SCHEDULED)
                    .build());
            String requesterToken = login("solicitante@empresa-a.test", "empresa-a");

            Map<String, Object> swap = new LinkedHashMap<>();
            swap.put("originalShiftId", original.getId().toString());
            swap.put("compensationDate", compensationDate.toString());
            HttpResult created = post("/api/v1/schedules/swap-requests", requesterToken, swap);
            assertThat(created.status()).isEqualTo(200);
            String swapId = created.json().get("id").asString();

            Map<String, Object> decision = new LinkedHashMap<>();
            decision.put("approved", true);
            decision.put("adminComments", "ok");

            List<HttpResult> results = inParallel(PARALLEL,
                    () -> patch("/api/v1/schedules/swap-requests/" + swapId + "/decision", managerToken, decision));

            assertThat(results).allSatisfy(r -> assertThat(r.status()).isNotEqualTo(500));
            assertThat(count(results, 200)).as("decisoes aceitas").isEqualTo(1);
            long compensations = workShiftRepository.findAll().stream()
                    .filter(s -> s.getEmployee().getId().equals(requester.getId()))
                    .filter(s -> s.getShiftDate().equals(compensationDate))
                    .filter(s -> s.getStatus() != ShiftStatus.CANCELLED)
                    .count();
            assertThat(compensations).as("escalas de compensacao").isEqualTo(1);
            assertThat(swapRepository.findAll()).hasSize(1);
        }
    }
}
