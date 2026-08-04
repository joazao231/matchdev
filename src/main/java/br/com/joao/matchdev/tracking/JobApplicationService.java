package br.com.joao.matchdev.tracking;

import java.util.List;
import java.util.UUID;

import br.com.joao.matchdev.auth.UserAccount;
import br.com.joao.matchdev.auth.UserAccountRepository;
import br.com.joao.matchdev.common.ResourceNotFoundException;
import br.com.joao.matchdev.job.JobPosting;
import br.com.joao.matchdev.job.JobPostingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final UserAccountRepository userRepository;
    private final JobPostingService jobService;

    public JobApplicationService(
            JobApplicationRepository applicationRepository,
            UserAccountRepository userRepository,
            JobPostingService jobService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobService = jobService;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(String email) {
        return applicationRepository.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse track(String email, ApplicationCreateRequest request) {
        UserAccount user = requireUser(email);
        JobPosting job = jobService.requireJob(request.jobId());
        JobApplication application = applicationRepository.findByUserAndJob(user, job)
                .orElseGet(() -> new JobApplication(user, job, request.status(), request.notes()));
        application.update(request.status(), request.notes());
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse update(String email, UUID id, ApplicationUpdateRequest request) {
        JobApplication application = applicationRepository.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));
        application.update(request.status(), request.notes());
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private UserAccount requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
