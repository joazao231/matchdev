package br.com.joao.matchdev.match;

import java.util.Set;

record CompatibilityResult(
        int score,
        MatchRecommendation recommendation,
        Set<String> matchedSkills,
        Set<String> missingRequiredSkills,
        Set<String> missingDesirableSkills,
        String explanation) {
}
