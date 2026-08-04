package br.com.joao.matchdev.integration.job;

import br.com.joao.matchdev.common.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class JobAnalyzerClient {

    private final RestClient restClient;

    public JobAnalyzerClient(
            @Value("${services.resume-parser.url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public JobAnalysisResponse analyze(String description) {
        try {
            JobAnalysisResponse response = restClient.post()
                    .uri("/api/v1/jobs/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JobAnalysisRequest(description))
                    .retrieve()
                    .body(JobAnalysisResponse.class);

            if (response == null) {
                throw new ExternalServiceException("O analisador de vagas devolveu uma resposta vazia");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(
                    "O analisador de vagas recusou a descrição (status "
                            + exception.getStatusCode().value() + ")",
                    exception);
        } catch (ResourceAccessException exception) {
            throw new ExternalServiceException("O serviço de análise de vagas está indisponível", exception);
        }
    }
}
