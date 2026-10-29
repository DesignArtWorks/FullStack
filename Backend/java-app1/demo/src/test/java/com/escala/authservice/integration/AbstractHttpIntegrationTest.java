package com.escala.authservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base dos testes de contrato HTTP: sobe o servidor real em porta aleatoria
 * (PostgreSQL e Redis em Testcontainers) e exercita as rotas pela rede, passando
 * por filtros de seguranca, serializacao e tratamento de erros.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractHttpIntegrationTest extends AbstractIntegrationTest {

    protected static final String TEST_PASSWORD = "Senha@123";
    protected static final String BFF_SECRET = "test-bff-shared-secret";
    protected static final String STRIPE_WEBHOOK_SECRET = "whsec_test_secret_for_integration";
    protected static final String CHATBOT_SECRET = "test-chatbot-webhook-secret";

    protected static final JsonMapper JSON = JsonMapper.builder().build();

    protected record HttpResult(int status, String body) {
        public JsonNode json() {
            return JSON.readTree(body);
        }
    }

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void registerHttpProperties(DynamicPropertyRegistry registry) {
        registry.add("application.security.public-endpoints.bff-shared-secret", () -> BFF_SECRET);
        registry.add("application.security.stripe.webhook-secret", () -> STRIPE_WEBHOOK_SECRET);
        registry.add("application.integrations.chatbot.webhook-secret", () -> CHATBOT_SECRET);
    }

    @BeforeEach
    void resetHttpState() {
        jdbcTemplate.execute("TRUNCATE TABLE marketing_leads, work_shifts, shift_swap_requests, time_records, "
                + "subscriptions, ai_usage_logs RESTART IDENTITY CASCADE");
    }

    protected HttpResult send(String method, String path, String token, Object body, Map<String, String> headers) {
        return sendRaw(method, path, token, body == null ? null : JSON.writeValueAsString(body), headers);
    }

    /**
     * Cliente HTTP/1.1 minimo sobre Socket. O HttpClient do JDK abre um pipe de loopback
     * que falha em alguns hosts Windows com antivirus; HttpURLConnection nao suporta PATCH.
     */
    protected HttpResult sendRaw(String method, String path, String token, String rawBody, Map<String, String> headers) {
        byte[] payload = rawBody == null ? new byte[0] : rawBody.getBytes(StandardCharsets.UTF_8);
        Map<String, String> all = new LinkedHashMap<>();
        all.put("Host", "localhost:" + port);
        all.put("Content-Type", "application/json");
        all.put("User-Agent", "escala-integration-test");
        all.put("Connection", "close");
        all.put("Content-Length", String.valueOf(payload.length));
        if (token != null) {
            all.put("Authorization", "Bearer " + token);
        }
        if (headers != null) {
            all.putAll(headers);
        }
        StringBuilder head = new StringBuilder(method).append(' ').append(path).append(" HTTP/1.1\r\n");
        all.forEach((name, value) -> head.append(name).append(": ").append(value).append("\r\n"));
        head.append("\r\n");

        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(30_000);
            OutputStream out = socket.getOutputStream();
            out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
            out.write(payload);
            out.flush();
            return parseResponse(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao chamar " + method + " " + path, exception);
        }
    }

    private HttpResult parseResponse(InputStream in) throws IOException {
        String statusLine = readLine(in);
        int status = Integer.parseInt(statusLine.split(" ")[1]);
        boolean chunked = false;
        int contentLength = -1;
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            String lower = line.toLowerCase();
            if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                chunked = true;
            } else if (lower.startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            }
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        if (chunked) {
            while (true) {
                int size = Integer.parseInt(readLine(in).split(";")[0].trim(), 16);
                if (size == 0) {
                    break;
                }
                body.write(in.readNBytes(size));
                readLine(in);
            }
        } else if (contentLength >= 0) {
            body.write(in.readNBytes(contentLength));
        } else {
            body.write(in.readAllBytes());
        }
        return new HttpResult(status, body.toString(StandardCharsets.UTF_8));
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        int current;
        while ((current = in.read()) != -1) {
            if (current == '\n') {
                break;
            }
            if (current != '\r') {
                line.append((char) current);
            }
        }
        return line.toString();
    }

    protected HttpResult get(String path, String token) {
        return send("GET", path, token, null, null);
    }

    protected HttpResult post(String path, String token, Object body) {
        return send("POST", path, token, body, null);
    }

    protected HttpResult patch(String path, String token, Object body) {
        return send("PATCH", path, token, body, null);
    }

    /** Autentica pelo endpoint real e devolve o JWT. */
    protected String login(String email, String companySlug) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", TEST_PASSWORD);
        body.put("companySlug", companySlug);
        HttpResult result = post("/api/v1/auth/authenticate", null, body);
        if (result.status() != 200) {
            throw new IllegalStateException("Login falhou: " + result.status() + " " + result.body());
        }
        return result.json().get("token").asString();
    }
}
