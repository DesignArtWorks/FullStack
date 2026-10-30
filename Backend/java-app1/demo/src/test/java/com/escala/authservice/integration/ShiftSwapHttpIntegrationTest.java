package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.SwapStatus;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP /api/v1/schedules/swap-requests: troca em duas etapas (colega e gestor)")
class ShiftSwapHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private ShiftSwapRequestRepository swapRepository;

    private Company company;
    private Sector sector;
    private WorkShift requesterShift;
    private final LocalDate shiftDate = LocalDate.now().plusDays(10);
    private final LocalDate compensationDate = LocalDate.now().plusDays(12);

    private String managerToken;
    private String requesterToken;
    private String colleagueToken;

    @BeforeEach
    void givenCompanyWithManagerRequesterAndColleague() {
        TenantFixture fixture = persistTenantFixture("empresa-a");
        company = fixture.company();
        sector = sectorRepository.saveAndFlush(Sector.builder().name("Atendimento").company(company).build());

        Employee requester = employeeIn("requester", "Solicitante");
        employeeIn("colleague", "Colega");
        requesterShift = workShiftRepository.saveAndFlush(WorkShift.builder()
                .employee(requester)
                .shiftDate(shiftDate)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .status(ShiftStatus.SCHEDULED)
                .build());

        managerToken = login("owner@empresa-a.test", "empresa-a");
        requesterToken = login("requester@empresa-a.test", "empresa-a");
        colleagueToken = login("colleague@empresa-a.test", "empresa-a");
    }

    private Employee employeeIn(String key, String name) {
        User user = persistUser(company, key + "@empresa-a.test", key + "-a", "USER");
        Employee employee = persistEmployee(company, user, user.getEmail(), name);
        employee.setSector(sector);
        return employeeRepository.saveAndFlush(employee);
    }

    private String createSwap(String token) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("originalShiftId", requesterShift.getId().toString());
        body.put("compensationDate", compensationDate.toString());
        body.put("comments", "Consulta medica");
        HttpResult result = post("/api/v1/schedules/swap-requests", token, body);
        assertThat(result.status()).isEqualTo(200);
        return result.json().get("id").asString();
    }

    private Map<String, Object> decision(boolean approved) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("approved", approved);
        body.put("adminComments", "Analisado");
        return body;
    }

    @Nested
    @DisplayName("fluxo feliz")
    class HappyPath {

        @Test
        @DisplayName("solicita, colega aprova, gestor aprova e a troca e efetivada com a compensacao agendada")
        void completesTwoStepApproval() {
            String swapId = createSwap(requesterToken);
            assertThat(swapRepository.findAll()).singleElement()
                    .extracting(s -> s.getStatus()).isEqualTo(SwapStatus.PENDING);

            HttpResult colleague = patch("/api/v1/schedules/swap-requests/" + swapId + "/colleague-approval",
                    colleagueToken, null);
            assertThat(colleague.status()).isEqualTo(200);
            assertThat(colleague.json().get("status").asString()).isEqualTo("COLLEAGUE_APPROVED");

            HttpResult manager = patch("/api/v1/schedules/swap-requests/" + swapId + "/decision",
                    managerToken, decision(true));
            assertThat(manager.status()).isEqualTo(200);
            assertThat(manager.json().get("status").asString()).isEqualTo("EFFECTIVE");

            assertThat(workShiftRepository.findById(requesterShift.getId()).orElseThrow().getStatus())
                    .isEqualTo(ShiftStatus.CANCELLED);
            assertThat(workShiftRepository.existsByEmployeeIdAndShiftDateAndStatusNot(
                    requesterShift.getEmployee().getId(), compensationDate, ShiftStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("gestor rejeita e a escala original permanece agendada")
        void managerRejection() {
            String swapId = createSwap(requesterToken);

            HttpResult rejected = patch("/api/v1/schedules/swap-requests/" + swapId + "/decision",
                    managerToken, decision(false));

            assertThat(rejected.status()).isEqualTo(200);
            assertThat(rejected.json().get("status").asString()).isEqualTo("REJECTED");
            assertThat(workShiftRepository.findById(requesterShift.getId()).orElseThrow().getStatus())
                    .isEqualTo(ShiftStatus.SCHEDULED);
        }
    }

    @Nested
    @DisplayName("regras de negocio")
    class BusinessRules {

        @Test
        @DisplayName("solicitante nao aprova a propria troca como colega")
        void requesterCannotSelfApprove() {
            String swapId = createSwap(requesterToken);

            HttpResult result = patch("/api/v1/schedules/swap-requests/" + swapId + "/colleague-approval",
                    requesterToken, null);

            assertThat(result.status()).isEqualTo(403);
            assertThat(swapRepository.findAll()).singleElement()
                    .extracting(s -> s.getStatus()).isEqualTo(SwapStatus.PENDING);
        }

        @Test
        @DisplayName("colega comum nao toma a decisao final do gestor")
        void colleagueCannotDecideAsManager() {
            String swapId = createSwap(requesterToken);

            HttpResult result = patch("/api/v1/schedules/swap-requests/" + swapId + "/decision",
                    colleagueToken, decision(true));

            assertThat(result.status()).isEqualTo(403);
            assertThat(workShiftRepository.findById(requesterShift.getId()).orElseThrow().getStatus())
                    .isEqualTo(ShiftStatus.SCHEDULED);
        }

        @Test
        @DisplayName("gestor nao decide duas vezes a mesma solicitacao")
        void cannotDecideTwice() {
            String swapId = createSwap(requesterToken);
            assertThat(patch("/api/v1/schedules/swap-requests/" + swapId + "/decision", managerToken,
                    decision(false)).status()).isEqualTo(200);

            HttpResult second = patch("/api/v1/schedules/swap-requests/" + swapId + "/decision",
                    managerToken, decision(true));

            assertThat(second.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
            assertThat(swapRepository.findAll()).singleElement()
                    .extracting(s -> s.getStatus()).isEqualTo(SwapStatus.REJECTED);
        }

        @Test
        @DisplayName("nao aceita troca de escala que nao pertence ao solicitante")
        void rejectsShiftOfAnotherEmployee() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("originalShiftId", requesterShift.getId().toString());
            body.put("compensationDate", compensationDate.toString());

            HttpResult result = post("/api/v1/schedules/swap-requests", colleagueToken, body);

            assertThat(result.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
            assertThat(swapRepository.count()).isZero();
        }

        @Test
        @DisplayName("nao aceita compensacao na mesma data da escala original")
        void rejectsSameDayCompensation() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("originalShiftId", requesterShift.getId().toString());
            body.put("compensationDate", shiftDate.toString());

            HttpResult result = post("/api/v1/schedules/swap-requests", requesterToken, body);

            assertThat(result.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
            assertThat(swapRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("isolamento entre empresas")
    class TenantIsolation {

        @Test
        @DisplayName("gestor e colaborador de outra empresa nao enxergam nem agem sobre a solicitacao")
        void otherTenantIsBlocked() {
            String swapId = createSwap(requesterToken);
            TenantFixture other = persistTenantFixture("empresa-b");
            User foreignUser = persistUser(other.company(), "user@empresa-b.test", "user-b", "USER");
            persistEmployee(other.company(), foreignUser, foreignUser.getEmail(), "Externo");
            String foreignManager = login("owner@empresa-b.test", "empresa-b");
            String foreignUserToken = login("user@empresa-b.test", "empresa-b");

            assertThat(get("/api/v1/schedules/swap-requests", foreignManager).json().size()).isZero();
            assertThat(patch("/api/v1/schedules/swap-requests/" + swapId + "/decision", foreignManager,
                    decision(true)).status()).isIn(403, 404);
            assertThat(patch("/api/v1/schedules/swap-requests/" + swapId + "/colleague-approval",
                    foreignUserToken, null).status()).isIn(403, 404);
            assertThat(swapRepository.findAll()).singleElement()
                    .extracting(s -> s.getStatus()).isEqualTo(SwapStatus.PENDING);
        }
    }
}
