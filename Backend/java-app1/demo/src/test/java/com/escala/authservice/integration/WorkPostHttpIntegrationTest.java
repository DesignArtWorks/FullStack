package com.escala.authservice.integration;

import com.escala.authservice.entity.Company;
import com.escala.authservice.entity.WorkPost;
import com.escala.authservice.repository.WorkPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HTTP /api/v1/work-posts: o servidor nao confia nos campos enviados pelo cliente")
class WorkPostHttpIntegrationTest extends AbstractHttpIntegrationTest {

    @Autowired
    private WorkPostRepository workPostRepository;

    private Company companyA;
    private Company companyB;
    private WorkPost postOfA;
    private String tokenB;

    @BeforeEach
    void seed() {
        companyA = persistTenantFixture("empresa-a").company();
        companyB = persistTenantFixture("empresa-b").company();
        companyA.setPlanType("PROFESSIONAL");
        companyB.setPlanType("PROFESSIONAL");
        companyRepository.saveAndFlush(companyA);
        companyRepository.saveAndFlush(companyB);
        postOfA = workPostRepository.saveAndFlush(WorkPost.builder().name("Posto A").company(companyA).build());
        tokenB = login("owner@empresa-b.test", "empresa-b");
    }

    @Test
    @DisplayName("id forjado no corpo nao sobrescreve o posto de outra empresa")
    void forgedIdDoesNotOverwriteForeignPost() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", postOfA.getId().toString());
        body.put("name", "Sequestrado");

        post("/api/v1/work-posts", tokenB, body);

        WorkPost stored = workPostRepository.findById(postOfA.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Posto A");
        assertThat(stored.getCompany().getId()).isEqualTo(companyA.getId());
    }

    @Test
    @DisplayName("empresa enviada no corpo e ignorada: o posto nasce na empresa do usuario autenticado")
    void companyFromBodyIsIgnored() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Posto B");
        body.put("company", Map.of("id", companyA.getId().toString()));

        HttpResult result = post("/api/v1/work-posts", tokenB, body);

        assertThat(result.status()).isEqualTo(200);
        WorkPost created = workPostRepository.findAll().stream()
                .filter(post -> "Posto B".equals(post.getName())).findFirst().orElseThrow();
        assertThat(created.getCompany().getId()).isEqualTo(companyB.getId());
    }

    @Test
    @DisplayName("a resposta nao expõe a entidade da empresa (plano, Stripe, geolocalizacao)")
    void responseDoesNotLeakCompanyEntity() {
        HttpResult result = post("/api/v1/work-posts", tokenB, Map.of("name", "Posto B"));

        assertThat(result.status()).isEqualTo(200);
        assertThat(result.body()).doesNotContain("planType", "stripe", "latitude", "slug");
        assertThat(result.json().has("company")).isFalse();
    }
}
