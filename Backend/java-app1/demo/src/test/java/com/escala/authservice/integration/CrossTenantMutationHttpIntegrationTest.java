package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.OperationalCapacity;
import com.escala.authservice.entity.Project;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.ShiftStatus;
import com.escala.authservice.entity.WorkPost;
import com.escala.authservice.entity.WorkShift;
import com.escala.authservice.repository.OperationalCapacityRepository;
import com.escala.authservice.repository.ProjectRepository;
import com.escala.authservice.repository.SectorRepository;
import com.escala.authservice.repository.WorkPostRepository;
import com.escala.authservice.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP: gestor da empresa B nao altera nem apaga recursos da empresa A pelo ID")
class CrossTenantMutationHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkPostRepository workPostRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private OperationalCapacityRepository capacityRepository;

    private Sector sector;
    private Project project;
    private WorkPost workPost;
    private WorkShift shift;
    private OperationalCapacity capacity;
    private Employee employeeA;
    private String tokenB;

    @BeforeEach
    void seedTwoTenants() {
        TenantFixture a = persistTenantFixture("empresa-a");
        persistTenantFixture("empresa-b");
        Company company = a.company();
        employeeA = a.employee();

        sector = sectorRepository.saveAndFlush(Sector.builder().name("Setor A").company(company).build());
        project = projectRepository.saveAndFlush(Project.builder().name("Projeto A").company(company).build());
        workPost = workPostRepository.saveAndFlush(WorkPost.builder().name("Posto A").company(company).build());
        shift = workShiftRepository.saveAndFlush(WorkShift.builder()
                .employee(employeeA)
                .shiftDate(LocalDate.now().plusDays(20))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .status(ShiftStatus.SCHEDULED)
                .build());
        capacity = capacityRepository.saveAndFlush(OperationalCapacity.builder()
                .targetId(sector.getId())
                .targetType("SECTOR")
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .minEmployeesRequired(1)
                .company(company)
                .build());

        tokenB = login("owner@empresa-b.test", "empresa-b");
    }

    private void assertDenied(HttpResult result) {
        assertThat(result.status()).isBetween(400, 404);
    }

    @Nested
    @DisplayName("exclusoes")
    class Deletes {

        @Test
        @DisplayName("setor, projeto e posto continuam existindo")
        void organizationResourcesSurvive() {
            assertDenied(send("DELETE", "/api/v1/organization/sectors/" + sector.getId(), tokenB, null, null));
            assertDenied(send("DELETE", "/api/v1/organization/projects/" + project.getId(), tokenB, null, null));
            assertDenied(send("DELETE", "/api/v1/work-posts/" + workPost.getId(), tokenB, null, null));

            assertThat(sectorRepository.findById(sector.getId())).isPresent();
            assertThat(projectRepository.findById(project.getId()).orElseThrow().isActive()).isTrue();
            assertThat(workPostRepository.findById(workPost.getId())).isPresent();
        }

        @Test
        @DisplayName("escala, capacidade e funcionario continuam ativos")
        void operationalResourcesSurvive() {
            assertDenied(send("DELETE", "/api/v1/escala/" + shift.getId(), tokenB, null, null));
            assertDenied(send("DELETE", "/api/v1/operational-capacities/" + capacity.getId(), tokenB, null, null));
            assertDenied(send("DELETE", "/api/v1/employees/" + employeeA.getId(), tokenB, null, null));

            assertThat(workShiftRepository.findById(shift.getId()).orElseThrow().getStatus())
                    .isEqualTo(ShiftStatus.SCHEDULED);
            assertThat(capacityRepository.findById(capacity.getId()).orElseThrow().isActive()).isTrue();
            assertThat(employeeRepository.findById(employeeA.getId()).orElseThrow().isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("atualizacoes")
    class Updates {

        @Test
        @DisplayName("setor e projeto mantem o nome original")
        void organizationNamesUnchanged() {
            assertDenied(send("PUT", "/api/v1/organization/sectors/" + sector.getId(), tokenB,
                    Map.of("name", "Invadido"), null));
            assertDenied(send("PUT", "/api/v1/organization/projects/" + project.getId(), tokenB,
                    Map.of("name", "Invadido"), null));

            assertThat(sectorRepository.findById(sector.getId()).orElseThrow().getName()).isEqualTo("Setor A");
            assertThat(projectRepository.findById(project.getId()).orElseThrow().getName()).isEqualTo("Projeto A");
        }

        @Test
        @DisplayName("capacidade operacional nao muda")
        void capacityUnchanged() {
            assertDenied(send("PUT", "/api/v1/operational-capacities/" + capacity.getId(), tokenB,
                    Map.of("targetId", sector.getId().toString(), "targetType", "SECTOR", "dayOfWeek", 2,
                            "startTime", "09:00:00", "endTime", "18:00:00", "minEmployeesRequired", 9), null));

            OperationalCapacity current = capacityRepository.findById(capacity.getId()).orElseThrow();
            assertThat(current.getDayOfWeek()).isEqualTo(1);
            assertThat(current.getMinEmployeesRequired()).isEqualTo(1);
        }
    }
}
