package br.com.joao.matchdev.match;

import java.util.List;
import java.util.UUID;

import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.candidate.CandidateProfileRepository;
import br.com.joao.matchdev.candidate.CandidateProfileService;
import br.com.joao.matchdev.job.JobPosting;
import br.com.joao.matchdev.job.JobPostingRepository;
import br.com.joao.matchdev.job.JobPostingService;
import br.com.joao.matchdev.job.JobStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {

    private final CandidateProfileService profileService;
    private final CandidateProfileRepository profileRepository;
    private final JobPostingService jobService;
    private final JobPostingRepository jobRepository;
    private final JobMatchRepository matchRepository;
    private final CompatibilityCalculator calculator;

    public MatchService(
            CandidateProfileService profileService,
            CandidateProfileRepository profileRepository,
            JobPostingService jobService,
            JobPostingRepository jobRepository,
            JobMatchRepository matchRepository,
            CompatibilityCalculator calculator) {
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.matchRepository = matchRepository;
        this.calculator = calculator;
    }

    @Transactional
    public JobMatchResponse analyze(String email, UUID jobId) {
        CandidateProfile profile = profileService.requireProfile(email);
        assertProfileComplete(profile);
        JobPosting job = jobService.requireJob(jobId);
        return JobMatchResponse.from(calculateAndSave(profile, job));
    }

    @Transactional
    public List<JobMatchResponse> analyzeAll(String email) {
        CandidateProfile profile = profileService.requireProfile(email);
        assertProfileComplete(profile);
        jobRepository.findAllByStatus(JobStatus.ACTIVE)
                .forEach(job -> calculateAndSave(profile, job));
        return list(email);
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponse> list(String email) {
        return matchRepository.findByProfileUserEmailIgnoreCaseOrderByScoreDesc(email).stream()
                .map(JobMatchResponse::from)
                .toList();
    }

    @Scheduled(
            initialDelayString = "${matchdev.automation.initial-delay-ms:60000}",
            fixedDelayString = "${matchdev.automation.interval-ms:1800000}")
    @Transactional
    public void refreshAllAutomatically() {
        List<JobPosting> jobs = jobRepository.findAllByStatus(JobStatus.ACTIVE);
        profileRepository.findAll().stream()
                .filter(this::isProfileComplete)
                .forEach(profile -> jobs.forEach(job -> calculateAndSave(profile, job)));
    }

    private JobMatch calculateAndSave(CandidateProfile profile, JobPosting job) {
        CompatibilityResult result = calculator.calculate(profile, job);
        JobMatch match = matchRepository.findByProfileAndJob(profile, job)
                .orElseGet(() -> new JobMatch(profile, job));
        match.update(result);
        return matchRepository.save(match);
    }

    private void assertProfileComplete(CandidateProfile profile) {
        if (!isProfileComplete(profile)) {
            throw new IllegalArgumentException("Complete o perfil profissional antes de analisar as vagas");
        }
    }

    private boolean isProfileComplete(CandidateProfile profile) {
        return profile.getDesiredRole() != null
                && !profile.getDesiredRole().isBlank()
                && profile.getDesiredSeniority() != null
                && !profile.getSkills().isEmpty()
                && !profile.getPreferredWorkModels().isEmpty();
    }
}
