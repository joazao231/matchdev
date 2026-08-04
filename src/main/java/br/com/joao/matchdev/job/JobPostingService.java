package br.com.joao.matchdev.job;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import br.com.joao.matchdev.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class JobPostingService {

    private final JobPostingRepository jobRepository;

    public JobPostingService(JobPostingRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse create(JobCreateRequest request) {
        String sourceUrl = StringUtils.hasText(request.sourceUrl()) ? request.sourceUrl().trim() : null;
        if (sourceUrl != null && jobRepository.findBySourceUrl(sourceUrl).isPresent()) {
            throw new IllegalArgumentException("Esta vaga já foi cadastrada");
        }

        JobPosting job = new JobPosting(
                request.title().trim(),
                request.company().trim(),
                request.description().trim(),
                sourceUrl,
                request.location().trim(),
                request.workModel(),
                request.seniority(),
                request.postedAt(),
                normalizeSkills(request.requiredSkills()),
                normalizeSkills(request.desirableSkills()));
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listActive(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Use página maior ou igual a 0 e tamanho entre 1 e 100");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobRepository.findByStatus(JobStatus.ACTIVE, pageable).map(JobResponse::from);
    }

    @Transactional(readOnly = true)
    public JobPosting requireJob(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));
    }

    @Transactional(readOnly = true)
    public JobResponse get(UUID id) {
        return JobResponse.from(requireJob(id));
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        Set<String> normalized = new LinkedHashSet<>();
        if (skills != null) {
            skills.forEach(skill -> normalized.add(normalize(skill)));
        }
        return normalized;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}
