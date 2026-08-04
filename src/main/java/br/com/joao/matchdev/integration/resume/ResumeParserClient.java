package br.com.joao.matchdev.integration.resume;

import java.io.IOException;

import br.com.joao.matchdev.common.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ResumeParserClient {

    private final RestClient restClient;

    public ResumeParserClient(
            @Value("${services.resume-parser.url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public ResumeAnalysisResponse analyze(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Envie um currículo em PDF");
        }

        try {
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_PDF);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(namedResource(file), fileHeaders));

            ResumeAnalysisResponse response = restClient.post()
                    .uri("/api/v1/resumes/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ResumeAnalysisResponse.class);

            if (response == null) {
                throw new ExternalServiceException("O analisador de currículos devolveu uma resposta vazia");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(
                    "O analisador de currículos recusou o arquivo (status "
                            + exception.getStatusCode().value() + ")",
                    exception);
        } catch (ResourceAccessException exception) {
            throw new ExternalServiceException("O serviço de análise de currículos está indisponível", exception);
        }
    }

    private ByteArrayResource namedResource(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            String fileName = file.getOriginalFilename() == null ? "curriculo.pdf" : file.getOriginalFilename();
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        } catch (IOException exception) {
            throw new IllegalArgumentException("Não foi possível ler o currículo enviado", exception);
        }
    }
}
