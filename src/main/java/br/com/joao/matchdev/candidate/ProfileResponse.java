package br.com.joao.matchdev.candidate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String fullName,
        String email,
        String headline,
        String desiredRole,
        String location,
        Seniority desiredSeniority,
        Set<String> skills,
        Set<WorkModel> preferredWorkModels,
        Instant updatedAt) {

    public static ProfileResponse from(CandidateProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getHeadline(),
                profile.getDesiredRole(),
                profile.getLocation(),
                profile.getDesiredSeniority(),
                profile.getSkills(),
                profile.getPreferredWorkModels(),
                profile.getUpdatedAt());
    }
}
