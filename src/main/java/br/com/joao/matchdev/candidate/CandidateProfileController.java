package br.com.joao.matchdev.candidate;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
public class CandidateProfileController {

    private final CandidateProfileService profileService;
    private final ResumeImportService resumeImportService;

    public CandidateProfileController(
            CandidateProfileService profileService,
            ResumeImportService resumeImportService) {
        this.profileService = profileService;
        this.resumeImportService = resumeImportService;
    }

    @GetMapping
    @Operation(summary = "Consulta o perfil profissional do usuário autenticado")
    ProfileResponse get(@AuthenticationPrincipal Jwt jwt) {
        return profileService.getByUserEmail(jwt.getSubject());
    }

    @PutMapping
    @Operation(summary = "Atualiza habilidades e preferências profissionais")
    ProfileResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.update(jwt.getSubject(), request);
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analisa um currículo em PDF e importa as habilidades encontradas")
    ResumeImportResponse importResume(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") MultipartFile file) {
        return resumeImportService.importResume(jwt.getSubject(), file);
    }
}
