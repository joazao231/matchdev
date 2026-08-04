package br.com.joao.matchdev.match;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.job.JobPosting;
import org.springframework.stereotype.Component;

@Component
public class CompatibilityCalculator {

    private static final double REQUIRED_SKILLS_WEIGHT = 60.0;
    private static final double DESIRABLE_SKILLS_WEIGHT = 15.0;
    private static final double ROLE_WEIGHT = 10.0;
    private static final double SENIORITY_WEIGHT = 10.0;
    private static final double WORK_MODEL_WEIGHT = 5.0;

    CompatibilityResult calculate(CandidateProfile profile, JobPosting job) {
        Set<String> candidateSkills = normalizeSet(profile.getSkills());
        Set<String> requiredSkills = normalizeSet(job.getRequiredSkills());
        Set<String> desirableSkills = normalizeSet(job.getDesirableSkills());

        Set<String> matchedRequired = intersection(requiredSkills, candidateSkills);
        Set<String> matchedDesirable = intersection(desirableSkills, candidateSkills);
        Set<String> missingRequired = difference(requiredSkills, candidateSkills);
        Set<String> missingDesirable = difference(desirableSkills, candidateSkills);

        double requiredPoints = ratio(matchedRequired.size(), requiredSkills.size()) * REQUIRED_SKILLS_WEIGHT;
        double desirablePoints = ratio(matchedDesirable.size(), desirableSkills.size()) * DESIRABLE_SKILLS_WEIGHT;
        double rolePoints = roleCompatibility(profile.getDesiredRole(), job.getTitle()) * ROLE_WEIGHT;
        double seniorityPoints = profile.getDesiredSeniority() == job.getSeniority() ? SENIORITY_WEIGHT : 0.0;
        double workModelPoints = profile.getPreferredWorkModels().contains(job.getWorkModel()) ? WORK_MODEL_WEIGHT : 0.0;

        int score = (int) Math.round(requiredPoints + desirablePoints + rolePoints
                + seniorityPoints + workModelPoints);
        MatchRecommendation recommendation = recommendationFor(score);

        Set<String> matchedSkills = new LinkedHashSet<>(matchedRequired);
        matchedSkills.addAll(matchedDesirable);

        String explanation = buildExplanation(
                score,
                matchedRequired.size(),
                requiredSkills.size(),
                missingRequired,
                recommendation);

        return new CompatibilityResult(
                score,
                recommendation,
                Set.copyOf(matchedSkills),
                Set.copyOf(missingRequired),
                Set.copyOf(missingDesirable),
                explanation);
    }

    private double ratio(int matched, int total) {
        return total == 0 ? 1.0 : (double) matched / total;
    }

    private double roleCompatibility(String desiredRole, String jobTitle) {
        Set<String> desiredTokens = tokens(desiredRole);
        Set<String> jobTokens = tokens(jobTitle);
        if (desiredTokens.isEmpty()) {
            return 0.0;
        }
        return ratio(intersection(desiredTokens, jobTokens).size(), desiredTokens.size());
    }

    private Set<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<String> ignored = Set.of("de", "da", "do", "e", "para", "the", "of");
        return Arrays.stream(normalize(value).split("[^a-z0-9+#.]+"))
                .filter(token -> token.length() > 1)
                .filter(token -> !ignored.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> normalizeSet(Set<String> values) {
        return values.stream()
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> intersection(Set<String> first, Set<String> second) {
        Set<String> result = new LinkedHashSet<>(first);
        result.retainAll(second);
        return result;
    }

    private Set<String> difference(Set<String> first, Set<String> second) {
        Set<String> result = new LinkedHashSet<>(first);
        result.removeAll(second);
        return result;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    private MatchRecommendation recommendationFor(int score) {
        if (score >= 80) {
            return MatchRecommendation.EXCELLENT;
        }
        if (score >= 65) {
            return MatchRecommendation.GOOD;
        }
        if (score >= 45) {
            return MatchRecommendation.POSSIBLE;
        }
        return MatchRecommendation.LOW;
    }

    private String buildExplanation(
            int score,
            int matchedRequired,
            int totalRequired,
            Set<String> missingRequired,
            MatchRecommendation recommendation) {
        String base = "Compatibilidade de %d%%: %d de %d habilidades obrigatórias atendidas."
                .formatted(score, matchedRequired, totalRequired);
        if (missingRequired.isEmpty()) {
            return base + " O perfil atende todas as habilidades obrigatórias. Recomendação: " + recommendation + ".";
        }
        return base + " Principais lacunas: " + String.join(", ", missingRequired)
                + ". Recomendação: " + recommendation + ".";
    }
}
