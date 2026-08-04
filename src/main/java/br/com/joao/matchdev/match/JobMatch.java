package br.com.joao.matchdev.match;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.job.JobPosting;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "job_matches",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_match_profile_job",
                columnNames = {"profile_id", "job_id"}))
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting job;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchRecommendation recommendation;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(nullable = false)
    private Instant analyzedAt;

    @ElementCollection
    @CollectionTable(name = "match_skills", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> matchedSkills = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "match_missing_required", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> missingRequiredSkills = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "match_missing_desirable", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private Set<String> missingDesirableSkills = new LinkedHashSet<>();

    protected JobMatch() {
    }

    public JobMatch(CandidateProfile profile, JobPosting job) {
        this.profile = profile;
        this.job = job;
    }

    public void update(CompatibilityResult result) {
        score = result.score();
        recommendation = result.recommendation();
        explanation = result.explanation();
        analyzedAt = Instant.now();
        replace(matchedSkills, result.matchedSkills());
        replace(missingRequiredSkills, result.missingRequiredSkills());
        replace(missingDesirableSkills, result.missingDesirableSkills());
    }

    private void replace(Set<String> target, Set<String> values) {
        target.clear();
        target.addAll(values);
    }

    public UUID getId() {
        return id;
    }

    public JobPosting getJob() {
        return job;
    }

    public int getScore() {
        return score;
    }

    public MatchRecommendation getRecommendation() {
        return recommendation;
    }

    public String getExplanation() {
        return explanation;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public Set<String> getMatchedSkills() {
        return Set.copyOf(matchedSkills);
    }

    public Set<String> getMissingRequiredSkills() {
        return Set.copyOf(missingRequiredSkills);
    }

    public Set<String> getMissingDesirableSkills() {
        return Set.copyOf(missingDesirableSkills);
    }
}
