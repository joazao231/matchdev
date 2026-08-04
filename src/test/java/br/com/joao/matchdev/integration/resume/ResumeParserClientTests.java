package br.com.joao.matchdev.integration.resume;

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
import org.springframework.mock.web.MockMultipartFile;

class ResumeParserClientTests {

    private HttpServer server;
    private ResumeParserClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/resumes/analyze", this::respondWithAnalysis);
        server.start();
        client = new ResumeParserClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldSendMultipartPdfAndReadThePythonContract() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "curriculo.pdf",
                "application/pdf",
                "%PDF-example".getBytes(StandardCharsets.UTF_8));

        ResumeAnalysisResponse response = client.analyze(file);

        assertThat(response.fileName()).isEqualTo("curriculo.pdf");
        assertThat(response.pageCount()).isEqualTo(1);
        assertThat(response.skills()).containsExactly("Java", "Spring Boot", "PostgreSQL");
        assertThat(response.suggestedRole()).isEqualTo("Desenvolvedor Backend");
    }

    private void respondWithAnalysis(HttpExchange exchange) throws IOException {
        assertThat(exchange.getRequestMethod()).isEqualTo("POST");
        assertThat(exchange.getRequestHeaders().getFirst("Content-Type")).startsWith("multipart/form-data");
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1);
        assertThat(requestBody).contains("curriculo.pdf", "%PDF-example");

        byte[] response = """
                {
                  "fileName": "curriculo.pdf",
                  "pageCount": 1,
                  "analyzedPageCount": 1,
                  "characterCount": 320,
                  "skills": ["Java", "Spring Boot", "PostgreSQL"],
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
