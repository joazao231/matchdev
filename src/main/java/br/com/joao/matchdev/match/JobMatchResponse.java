package br.com.joao.matchdev.match;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.candidate.WorkModel;

public record JobMatchResponse(
        UUID id,
        UUID jobId,
        String jobTitle,
        String company,
        String location,
        WorkModel workModel,
        String sourceUrl,
        int score,
        MatchRecommendation recommendation,
        Set<String> matchedSkills,
        Set<String> missingRequiredSkills,
        Set<String> missingDesirableSkills,
        String explanation,
        Instant analyzedAt) {

    public static JobMatchResponse from(JobMatch match) {
        return new JobMatchResponse(
                match.getId(),
                match.getJob().getId(),
                match.getJob().getTitle(),
                match.getJob().getCompany(),
                match.getJob().getLocation(),
                match.getJob().getWorkModel(),
                match.getJob().getSourceUrl(),
                match.getScore(),
                match.getRecommendation(),
                match.getMatchedSkills(),
                match.getMissingRequiredSkills(),
                match.getMissingDesirableSkills(),
                match.getExplanation(),
                match.getAnalyzedAt());
    }
}
