package br.com.joao.matchdev.tracking;

import java.time.Instant;
import java.util.UUID;

import br.com.joao.matchdev.candidate.WorkModel;

public record ApplicationResponse(
        UUID id,
        UUID jobId,
        String jobTitle,
        String company,
        String location,
        WorkModel workModel,
        String sourceUrl,
        ApplicationStatus status,
        String notes,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt) {

    public static ApplicationResponse from(JobApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany(),
                application.getJob().getLocation(),
                application.getJob().getWorkModel(),
                application.getJob().getSourceUrl(),
                application.getStatus(),
                application.getNotes(),
                application.getAppliedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
