package br.com.joao.matchdev.match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.job.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobMatchRepository extends JpaRepository<JobMatch, UUID> {

    Optional<JobMatch> findByProfileAndJob(CandidateProfile profile, JobPosting job);

    List<JobMatch> findByProfileUserEmailIgnoreCaseOrderByScoreDesc(String email);
}
