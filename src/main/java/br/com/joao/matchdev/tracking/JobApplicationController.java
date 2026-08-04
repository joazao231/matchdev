package br.com.joao.matchdev.tracking;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    public JobApplicationController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "Lista as candidaturas do usuário autenticado")
    List<ApplicationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return applicationService.list(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona ou atualiza uma vaga no funil de candidaturas")
    ApplicationResponse track(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApplicationCreateRequest request) {
        return applicationService.track(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Move uma candidatura para outra etapa")
    ApplicationResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationUpdateRequest request) {
        return applicationService.update(jwt.getSubject(), id, request);
    }
}
