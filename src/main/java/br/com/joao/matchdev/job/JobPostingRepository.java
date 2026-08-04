package br.com.joao.matchdev.job;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {

    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    List<JobPosting> findAllByStatus(JobStatus status);

    Optional<JobPosting> findBySourceUrl(String sourceUrl);
}
