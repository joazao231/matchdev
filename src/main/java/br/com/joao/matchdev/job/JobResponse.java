package br.com.joao.matchdev.job;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;

public record JobResponse(
        UUID id,
        String title,
        String company,
        String description,
        String sourceUrl,
        String location,
        WorkModel workModel,
        Seniority seniority,
        JobStatus status,
        LocalDate postedAt,
        Set<String> requiredSkills,
        Set<String> desirableSkills,
        Instant createdAt) {

    public static JobResponse from(JobPosting job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getDescription(),
                job.getSourceUrl(),
                job.getLocation(),
                job.getWorkModel(),
                job.getSeniority(),
                job.getStatus(),
                job.getPostedAt(),
                job.getRequiredSkills(),
                job.getDesirableSkills(),
                job.getCreatedAt());
    }
}
