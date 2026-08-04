package br.com.joao.matchdev.candidate;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.auth.UserAccount;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(length = 180)
    private String headline;

    @Column(length = 120)
    private String desiredRole;

    @Column(length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Seniority desiredSeniority;

    @ElementCollection
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> skills = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "candidate_work_models", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "work_model", nullable = false, length = 30)
    private Set<WorkModel> preferredWorkModels = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CandidateProfile() {
    }

    public CandidateProfile(UserAccount user) {
        this.user = user;
    }

    public void update(
            String headline,
            String desiredRole,
            String location,
            Seniority desiredSeniority,
            Set<String> skills,
            Set<WorkModel> preferredWorkModels) {
        this.headline = headline.trim();
        this.desiredRole = desiredRole.trim();
        this.location = location.trim();
        this.desiredSeniority = desiredSeniority;
        this.skills.clear();
        this.skills.addAll(skills);
        this.preferredWorkModels.clear();
        this.preferredWorkModels.addAll(preferredWorkModels);
    }

    public int mergeResumeSkills(Set<String> detectedSkills, String suggestedRole) {
        int previousSize = skills.size();
        skills.addAll(detectedSkills);

        if ((desiredRole == null || desiredRole.isBlank()) && suggestedRole != null && !suggestedRole.isBlank()) {
            desiredRole = suggestedRole.trim();
        }
        return skills.size() - previousSize;
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

    public UUID getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDesiredRole() {
        return desiredRole;
    }

    public String getLocation() {
        return location;
    }

    public Seniority getDesiredSeniority() {
        return desiredSeniority;
    }

    public Set<String> getSkills() {
        return Set.copyOf(skills);
    }

    public Set<WorkModel> getPreferredWorkModels() {
        return Set.copyOf(preferredWorkModels);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
