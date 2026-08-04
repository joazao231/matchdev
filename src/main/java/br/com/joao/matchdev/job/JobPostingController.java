package br.com.joao.matchdev.job;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobPostingController {

    private final JobPostingService jobService;
    private final JobImportService jobImportService;

    public JobPostingController(JobPostingService jobService, JobImportService jobImportService) {
        this.jobService = jobService;
        this.jobImportService = jobImportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma vaga encontrada por uma automação ou pelo usuário")
    JobResponse create(@Valid @RequestBody JobCreateRequest request) {
        return jobService.create(request);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Importa uma descrição, extrai tecnologias e calcula o match")
    JobImportResponse importAndAnalyze(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JobImportRequest request) {
        return jobImportService.importAndAnalyze(jwt.getSubject(), request);
    }

    @GetMapping
    @Operation(summary = "Lista vagas ativas")
    Page<JobResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobService.listActive(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta os detalhes de uma vaga")
    JobResponse get(@PathVariable UUID id) {
        return jobService.get(id);
    }
}
