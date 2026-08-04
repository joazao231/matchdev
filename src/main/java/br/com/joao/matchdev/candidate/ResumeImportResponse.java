package br.com.joao.matchdev.candidate;

import br.com.joao.matchdev.integration.resume.ResumeAnalysisResponse;

public record ResumeImportResponse(
        ResumeAnalysisResponse analysis,
        ProfileResponse profile,
        int newSkillsImported) {
}
