package br.com.joao.matchdev.job;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 600)
    private String sourceUrl;

    @Column(nullable = false, length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkModel workModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Seniority seniority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status;

    private LocalDate postedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> requiredSkills = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "job_desirable_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> desirableSkills = new LinkedHashSet<>();

    protected JobPosting() {
    }

    public JobPosting(
            String title,
            String company,
            String description,
            String sourceUrl,
            String location,
            WorkModel workModel,
            Seniority seniority,
            LocalDate postedAt,
            Set<String> requiredSkills,
            Set<String> desirableSkills) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.sourceUrl = sourceUrl;
        this.location = location;
        this.workModel = workModel;
        this.seniority = seniority;
        this.postedAt = postedAt;
        this.requiredSkills.addAll(requiredSkills);
        this.desirableSkills.addAll(desirableSkills);
        this.status = JobStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) {
            status = JobStatus.ACTIVE;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getLocation() {
        return location;
    }

    public WorkModel getWorkModel() {
        return workModel;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public JobStatus getStatus() {
        return status;
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<String> getRequiredSkills() {
        return Set.copyOf(requiredSkills);
    }

    public Set<String> getDesirableSkills() {
        return Set.copyOf(desirableSkills);
    }
}
