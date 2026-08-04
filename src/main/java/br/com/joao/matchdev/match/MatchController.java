package br.com.joao.matchdev.match;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/jobs/{jobId}")
    @Operation(summary = "Calcula a compatibilidade do perfil com uma vaga")
    JobMatchResponse analyze(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID jobId) {
        return matchService.analyze(jwt.getSubject(), jobId);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Recalcula a compatibilidade com todas as vagas ativas")
    List<JobMatchResponse> refresh(@AuthenticationPrincipal Jwt jwt) {
        return matchService.analyzeAll(jwt.getSubject());
    }

    @GetMapping
    @Operation(summary = "Lista as vagas da maior para a menor compatibilidade")
    List<JobMatchResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return matchService.list(jwt.getSubject());
    }
}
