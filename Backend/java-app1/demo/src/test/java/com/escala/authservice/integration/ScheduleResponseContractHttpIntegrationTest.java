package com.escala.authservice.integration;

import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.OperationalCapacity;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.User;
import com.escala.authservice.entity.WorkShift;
import com.escala.authservice.repository.OperationalCapacityRepository;
import com.escala.authservice.repository.SectorRepository;
import com.escala.authservice.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato de resposta: escalas, trocas e capacidades nao expõem entidades de persistencia")
class ScheduleResponseContractHttpIntegrationTest extends AbstractHttpIntegrationTest {

    /** Campos que pertencem a entidade/empresa/usuario e nunca devem sair nestas respostas. */
    private static final String[] FORBIDDEN = {
            "\"company\"", "planType", "stripe", "\"password\"", "\"email\"", "\"roles\"", "latitude", "\"user\""
    };

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private OperationalCapacityRepository capacityRepository;

    private WorkShift shift;
    private Sector sector;
    private String managerToken;
    private String requesterToken;

    @BeforeEach
    void seed() {
        TenantFixture fixture = persistTenantFixture("empresa-a");
        sector = sectorRepository.saveAndFlush(Sector.builder().name("Atendimento").company(fixture.company()).build());
        User user = persistUser(fixture.company(), "solicitante@empresa-a.test", "solicitante-a", "USER");
        Employee requester = persistEmployee(fixture.company(), user, user.getEmail(), "Solicitante");
        requester.setSector(sector);
        employeeRepository.saveAndFlush(requester);
        shift = workShiftRepository.saveAndFlush(WorkShift.builder()
                .employee(requester)
                .shiftDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .status(ShiftStatus.SCHEDULED)
                .build());
        managerToken = login("owner@empresa-a.test", "empresa-a");
        requesterToken = login("solicitante@empresa-a.test", "empresa-a");
    }

    @Test
    @DisplayName("solicitacao de troca devolve so os campos do contrato")
    void swapResponseHasOnlyContractFields() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("originalShiftId", shift.getId().toString());
        body.put("compensationDate", LocalDate.now().plusDays(12).toString());

        HttpResult created = post("/api/v1/schedules/swap-requests", requesterToken, body);

        assertThat(created.status()).isEqualTo(200);
        assertThat(created.json().get("status").asString()).isEqualTo("PENDING");
        assertThat(created.json().get("requester").get("fullName").asString()).isEqualTo("Solicitante");
        assertThat(created.json().get("originalShift").get("shiftDate").asString())
                .isEqualTo(shift.getShiftDate().toString());
        assertThat(created.body()).doesNotContain(FORBIDDEN);

        HttpResult listed = get("/api/v1/schedules/swap-requests", managerToken);
        assertThat(listed.status()).isEqualTo(200);
        assertThat(listed.json().size()).isEqualTo(1);
        assertThat(listed.body()).doesNotContain(FORBIDDEN);
    }

    @Test
    @DisplayName("listagem mensal de escalas devolve so os campos do contrato")
    void monthlyListHasOnlyContractFields() {
        LocalDate date = shift.getShiftDate();

        HttpResult result = get("/api/v1/schedules?year=" + date.getYear() + "&month=" + date.getMonthValue(),
                managerToken);

        assertThat(result.status()).isEqualTo(200);
        assertThat(result.json().size()).isEqualTo(1);
        assertThat(result.json().get(0).get("employee").get("fullName").asString()).isEqualTo("Solicitante");
        assertThat(result.body()).doesNotContain(FORBIDDEN);
    }

    @Test
    @DisplayName("capacidade operacional devolve so os campos do contrato")
    void capacityResponseHasOnlyContractFields() {
        capacityRepository.saveAndFlush(OperationalCapacity.builder()
                .targetId(sector.getId())
                .targetType("SECTOR")
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .minEmployeesRequired(2)
                .company(sector.getCompany())
                .build());

        HttpResult result = get("/api/v1/operational-capacities", managerToken);

        assertThat(result.status()).isEqualTo(200);
        assertThat(result.json().size()).isEqualTo(1);
        assertThat(result.json().get(0).get("minEmployeesRequired").asInt()).isEqualTo(2);
        assertThat(result.body()).doesNotContain(FORBIDDEN);
    }
}
