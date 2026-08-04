package br.com.joao.matchdev.integration.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobAnalyzerClientTests {

    private HttpServer server;
    private JobAnalyzerClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/jobs/analyze", this::respondWithAnalysis);
        server.start();
        client = new JobAnalyzerClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldSendDescriptionAndReadTheSkillClassificationContract() {
        JobAnalysisResponse response = client.analyze(
                "Buscamos Java e Spring Boot. Docker será um diferencial para o time.");

        assertThat(response.requiredSkills()).containsExactly("Java", "Spring Boot");
        assertThat(response.desirableSkills()).containsExactly("Docker");
        assertThat(response.suggestedRole()).isEqualTo("Desenvolvedor Backend");
    }

    private void respondWithAnalysis(HttpExchange exchange) throws IOException {
        assertThat(exchange.getRequestMethod()).isEqualTo("POST");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).startsWith("application/json");
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(requestBody).contains("description", "Java", "Docker");

        byte[] response = """
                {
                  "characterCount": 72,
                  "requiredSkills": ["Java", "Spring Boot"],
                  "desirableSkills": ["Docker"],
                  "suggestedRole": "Desenvolvedor Backend",
                  "warnings": []
                }
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
