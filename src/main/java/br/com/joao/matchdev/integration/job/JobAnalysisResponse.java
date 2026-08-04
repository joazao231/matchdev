package br.com.joao.matchdev.integration.job;

import java.util.List;

public record JobAnalysisResponse(
        int characterCount,
        List<String> requiredSkills,
        List<String> desirableSkills,
        String suggestedRole,
        List<String> warnings) {
}
