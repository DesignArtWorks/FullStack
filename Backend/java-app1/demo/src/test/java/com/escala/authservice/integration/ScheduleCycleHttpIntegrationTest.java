package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.ScheduleCycleAssignment;
import com.escala.authservice.entity.User;
import com.escala.authservice.repository.ScheduleCycleAssignmentRepository;
import com.escala.authservice.repository.ScheduleCycleRepository;
import com.escala.authservice.scheduling.domain.enums.ModalidadeTrabalho;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP /api/v1/scheduling/cycles: ciclo mensal rascunho -> publicacao -> retificacao -> arquivamento")
class ScheduleCycleHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private ScheduleCycleRepository scheduleCycleRepository;

    @Autowired
    private ScheduleCycleAssignmentRepository assignmentRepository;

    private Map<String, Object> cycleRequest(int year, int month) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("year", year);
        body.put("month", month);
        body.put("timezone", "America/Sao_Paulo");
        return body;
    }

    private String createCycle(String token, int year, int month) {
        HttpResult created = post("/api/v1/scheduling/cycles", token, cycleRequest(year, month));
        assertThat(created.status()).isEqualTo(200);
        return created.json().get("id").asString();
    }

    private void addAssignment(String cycleId, Company company, Employee employee) {
        var cycle = scheduleCycleRepository.findByCompanyIdAndPublicId(company.getId(), UUID.fromString(cycleId)).orElseThrow();
        assignmentRepository.saveAndFlush(ScheduleCycleAssignment.builder()
                .cycle(cycle)
                .company(company)
                .employee(employee)
                .assignmentDate(LocalDate.of(2026, 7, 6))
                .legendCode("TRAB")
                .legendLabel("Trabalho")
                .legendImpact("WORKED")
                .plannedMinutes(480)
                .modality(ModalidadeTrabalho.PRESENCIAL)
                .build());
    }

    @Nested
    @DisplayName("quando o gestor (OWNER) conduz o ciclo")
    class ManagerLifecycle {

        @Test
        @DisplayName("cria o ciclo em rascunho e o lista")
        void createsDraftAndLists() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");

            HttpResult created = post("/api/v1/scheduling/cycles", token, cycleRequest(2026, 7));

            assertThat(created.status()).isEqualTo(200);
            assertThat(created.json().get("status").asString()).isEqualTo("RASCUNHO");
            assertThat(created.json().get("year").asInt()).isEqualTo(2026);

            HttpResult list = get("/api/v1/scheduling/cycles", token);
            assertThat(list.status()).isEqualTo(200);
            assertThat(list.json().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("publica, retifica (versao 2) e arquiva pelo contrato HTTP")
        void publishesRectifiesAndArchives() {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");
            String cycleId = createCycle(token, 2026, 7);
            addAssignment(cycleId, fixture.company(), fixture.employee());

            HttpResult published = post("/api/v1/scheduling/cycles/" + cycleId + "/publish", token, null);
            assertThat(published.status()).isEqualTo(200);
            assertThat(published.json().get("status").asString()).isEqualTo("PUBLICADO");
            assertThat(published.json().get("publishedAt").isNull()).isFalse();

            HttpResult rectified = post("/api/v1/scheduling/cycles/" + cycleId + "/rectify", token, null);
            assertThat(rectified.status()).isEqualTo(200);
            assertThat(rectified.json().get("status").asString()).isEqualTo("RETIFICADO");
            assertThat(rectified.json().get("businessVersion").asInt()).isEqualTo(2);

            HttpResult archived = post("/api/v1/scheduling/cycles/" + cycleId + "/archive", token, null);
            assertThat(archived.status()).isEqualTo(200);
            assertThat(archived.json().get("status").asString()).isEqualTo("ARQUIVADO");
        }

        @Test
        @DisplayName("nao arquiva um ciclo que ainda esta em rascunho")
        void rejectsArchivingDraft() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");
            String cycleId = createCycle(token, 2026, 8);

            HttpResult archived = post("/api/v1/scheduling/cycles/" + cycleId + "/archive", token, null);

            assertThat(archived.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
            HttpResult current = get("/api/v1/scheduling/cycles/" + cycleId, token);
            assertThat(current.json().get("status").asString()).isEqualTo("RASCUNHO");
        }

        @Test
        @DisplayName("nao rascunha dois ciclos para o mesmo mes")
        void rejectsDuplicateMonth() {
            persistTenantFixture("empresa-a");
            String token = login("owner@empresa-a.test", "empresa-a");
            createCycle(token, 2026, 9);

            HttpResult second = post("/api/v1/scheduling/cycles", token, cycleRequest(2026, 9));

            assertThat(second.status()).isGreaterThanOrEqualTo(400).isLessThan(500);
            assertThat(scheduleCycleRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("autorizacao e isolamento entre empresas")
    class Authorization {

        @Test
        @DisplayName("colaborador comum nao cria nem publica ciclos")
        void regularEmployeeCannotManage() {
            TenantFixture fixture = persistTenantFixture("empresa-a");
            User regular = persistUser(fixture.company(), "user@empresa-a.test", "user-a", "USER");
            persistEmployee(fixture.company(), regular, regular.getEmail(), "Colaborador");
            String ownerToken = login("owner@empresa-a.test", "empresa-a");
            String cycleId = createCycle(ownerToken, 2026, 7);
            String userToken = login("user@empresa-a.test", "empresa-a");

            assertThat(post("/api/v1/scheduling/cycles", userToken, cycleRequest(2026, 10)).status()).isEqualTo(403);
            assertThat(post("/api/v1/scheduling/cycles/" + cycleId + "/publish", userToken, null).status())
                    .isEqualTo(403);
            assertThat(scheduleCycleRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("gestor de outra empresa nao le nem altera o ciclo (sem vazar existencia)")
        void otherTenantCannotAccessCycle() {
            persistTenantFixture("empresa-a");
            persistTenantFixture("empresa-b");
            String tokenA = login("owner@empresa-a.test", "empresa-a");
            String tokenB = login("owner@empresa-b.test", "empresa-b");
            String cycleId = createCycle(tokenA, 2026, 7);

            HttpResult read = get("/api/v1/scheduling/cycles/" + cycleId, tokenB);
            HttpResult publish = post("/api/v1/scheduling/cycles/" + cycleId + "/publish", tokenB, null);
            HttpResult archive = post("/api/v1/scheduling/cycles/" + cycleId + "/archive", tokenB, null);

            // Contrato atual: ciclo de outra empresa e tratado como "nao encontrado" (400), sem vazar dados.
            assertThat(read.status()).isIn(400, 403, 404);
            assertThat(publish.status()).isIn(400, 403, 404);
            assertThat(archive.status()).isIn(400, 403, 404);
            assertThat(read.body()).doesNotContain("RASCUNHO", "businessVersion");
            assertThat(get("/api/v1/scheduling/cycles", tokenB).json().size()).isZero();
            assertThat(get("/api/v1/scheduling/cycles/" + cycleId, tokenA).json().get("status").asString())
                    .isEqualTo("RASCUNHO");
        }
    }
}
