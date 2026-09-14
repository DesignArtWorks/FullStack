package com.escala.authservice.integration;

import com.escala.authservice.entity.User;
import com.escala.authservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AvatarAuthorizationIntegrationTest extends AbstractIntegrationTest {
    @LocalServerPort int port;
    @Autowired JwtService jwtService;

    @Test
    void allowsSelfAndOrdinaryColleaguesButRejectsCrossTenantAndAnonymous() throws Exception {
        var a = persistTenantFixture("avatar-a");
        var b = persistTenantFixture("avatar-b");
        User colleague = persistUser(a.company(), "colleague@example.test", "colleague", "USER");
        String ownToken = token(a.owner());
        var own = request(a.owner().getId(), ownToken);
        assertThat(own.statusCode()).isEqualTo(200);
        assertThat(own.headers().firstValue("Cache-Control")).contains("no-store");
        assertThat(own.body()).contains(a.owner().getId().toString(), a.company().getId().toString())
                .doesNotContain("email", "password", "roles");
        assertThat(request(a.owner().getId(), token(colleague)).statusCode()).isEqualTo(200);
        assertThat(request(a.owner().getId(), token(b.owner())).statusCode()).isEqualTo(404);
        assertThat(request(UUID.randomUUID(), token(b.owner())).statusCode()).isEqualTo(404);
        assertThat(request(a.owner().getId(), null).statusCode()).isEqualTo(401);
    }

    @Test
    void existingTokenDoesNotBypassDisabledMembershipOrMovedAvatarOwner() throws Exception {
        var a = persistTenantFixture("avatar-current");
        var b = persistTenantFixture("avatar-other");
        User colleague = persistUser(a.company(), "peer@example.test", "peer", "USER");
        String existingToken = token(colleague);
        assertThat(request(a.owner().getId(), existingToken).statusCode()).isEqualTo(200);
        a.owner().setCompany(b.company());
        userRepository.saveAndFlush(a.owner());
        assertThat(request(a.owner().getId(), existingToken).statusCode()).isEqualTo(404);
        colleague.setActive(false);
        userRepository.saveAndFlush(colleague);
        assertThat(request(colleague.getId(), existingToken).statusCode()).isEqualTo(401);
    }

    private String token(User user) {
        var details = org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password("test-only-unused").authorities("USER").build();
        // Deliberately forged tenant/role hints: the real filter must reload identity.
        return jwtService.generateToken(Map.of("id", user.getId().toString(), "companyId", UUID.randomUUID().toString(),
                "roles", java.util.List.of("SYSTEM_ADMIN")), details);
    }

    private HttpResponse<String> request(UUID ownerId, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/users/" + ownerId + "/avatar-access"))
                .header("X-Tenant-ID", UUID.randomUUID().toString()).GET();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
