package br.com.joao.matchdev.tracking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.joao.matchdev.auth.UserAccount;
import br.com.joao.matchdev.job.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Optional<JobApplication> findByUserAndJob(UserAccount user, JobPosting job);

    List<JobApplication> findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Optional<JobApplication> findByIdAndUserEmailIgnoreCase(UUID id, String email);
}
