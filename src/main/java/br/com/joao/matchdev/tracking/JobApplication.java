package br.com.joao.matchdev.tracking;

import java.time.Instant;
import java.util.UUID;

import br.com.joao.matchdev.auth.UserAccount;
import br.com.joao.matchdev.job.JobPosting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_application_user_job",
                columnNames = {"user_id", "job_id"}))
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(length = 1000)
    private String notes;

    private Instant appliedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected JobApplication() {
    }

    public JobApplication(UserAccount user, JobPosting job, ApplicationStatus status, String notes) {
        this.user = user;
        this.job = job;
        update(status, notes);
    }

    public void update(ApplicationStatus nextStatus, String notes) {
        this.status = nextStatus;
        this.notes = notes == null || notes.isBlank() ? null : notes.trim();
        if (appliedAt == null && isApplicationStarted(nextStatus)) {
            appliedAt = Instant.now();
        }
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    private boolean isApplicationStarted(ApplicationStatus nextStatus) {
        return nextStatus == ApplicationStatus.APPLIED
                || nextStatus == ApplicationStatus.INTERVIEW
                || nextStatus == ApplicationStatus.OFFER;
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public JobPosting getJob() {
        return job;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
