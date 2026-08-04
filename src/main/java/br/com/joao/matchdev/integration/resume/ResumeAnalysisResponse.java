package br.com.joao.matchdev.integration.resume;

import java.util.List;

public record ResumeAnalysisResponse(
        String fileName,
        int pageCount,
        int analyzedPageCount,
        int characterCount,
        List<String> skills,
        String suggestedRole,
        List<String> warnings) {
}
