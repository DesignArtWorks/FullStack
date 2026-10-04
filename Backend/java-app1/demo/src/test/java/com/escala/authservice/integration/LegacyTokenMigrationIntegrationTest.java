package com.escala.authservice.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = false)
class LegacyTokenMigrationIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void forwardMigrationRevokesRecoverableTokensAndPreservesHashOnlyTokens() {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("2026070101").load().migrate();
        JdbcTemplate db = new JdbcTemplate(new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
        db.execute("""
                INSERT INTO companies (id, active, name, slug)
                VALUES ('10000000-0000-0000-0000-000000000001', true, 'Migration fixture', 'migration-fixture');
                INSERT INTO users (id, active, company_id, email, password, username)
                VALUES ('20000000-0000-0000-0000-000000000001', true,
                '10000000-0000-0000-0000-000000000001', 'fixture@example.test', 'test-only-unused', 'fixture');
                """);
        for (int i = 1; i <= 3; i++) {
            String id = "30000000-0000-0000-0000-00000000000" + i;
            String plaintext = i < 3 ? "test-only-legacy-" + i : null;
            String hash = String.format("%064x", i);
            db.update("""
                    INSERT INTO team_invitations (id, active, expires_at, company_id, invited_by_id,
                    token_hash, token_preview, token, email, role_name, accepted_at)
                    VALUES (?::uuid, ?, CURRENT_TIMESTAMP + INTERVAL '1 day',
                    '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
                    ?, 'abcdef', ?, 'invite@example.test', 'USER', CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END)
                    """, id, i != 2, hash, plaintext, i == 2);
            db.update("""
                    INSERT INTO password_reset_tokens (id, expires_at, user_id, token_hash, token_preview, token, used_at)
                    VALUES (?::uuid, CURRENT_TIMESTAMP + INTERVAL '1 day', '20000000-0000-0000-0000-000000000001',
                    ?, 'abcdef', ?, CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END)
                    """, id, hash, plaintext, i == 2);
        }
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()).load().migrate();
        assertThat(db.queryForObject("""
                SELECT count(*) FROM information_schema.columns WHERE table_schema = 'public'
                AND table_name IN ('team_invitations', 'password_reset_tokens') AND column_name = 'token'
                """, Integer.class)).isZero();
        assertThat(db.queryForObject("SELECT count(*) FROM team_invitations WHERE active AND expires_at > CURRENT_TIMESTAMP", Integer.class)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM password_reset_tokens WHERE used_at IS NULL AND expires_at > CURRENT_TIMESTAMP", Integer.class)).isEqualTo(1);
        for (String table : new String[]{"team_invitations", "password_reset_tokens"}) {
            assertThat(db.queryForObject("SELECT token_preview FROM " + table + " WHERE id = '30000000-0000-0000-0000-000000000003'", String.class)).isEqualTo("abcdef");
            assertThat(db.queryForObject("SELECT count(*) FROM " + table + " WHERE token_preview IS NULL", Integer.class)).isEqualTo(2);
            assertThat(db.queryForList("SELECT token_hash FROM " + table + " ORDER BY id", String.class))
                    .containsExactly(String.format("%064x", 1), String.format("%064x", 2), String.format("%064x", 3));
        }
    }
}
