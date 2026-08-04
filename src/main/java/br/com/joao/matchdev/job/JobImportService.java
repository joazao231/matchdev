package br.com.joao.matchdev.job;

import java.util.LinkedHashSet;

import br.com.joao.matchdev.integration.job.JobAnalysisResponse;
import br.com.joao.matchdev.integration.job.JobAnalyzerClient;
import br.com.joao.matchdev.match.JobMatchResponse;
import br.com.joao.matchdev.match.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobImportService {

    private final JobAnalyzerClient analyzerClient;
    private final JobPostingService jobService;
    private final MatchService matchService;

    public JobImportService(
            JobAnalyzerClient analyzerClient,
            JobPostingService jobService,
            MatchService matchService) {
        this.analyzerClient = analyzerClient;
        this.jobService = jobService;
        this.matchService = matchService;
    }

    @Transactional
    public JobImportResponse importAndAnalyze(String email, JobImportRequest request) {
        JobAnalysisResponse analysis = analyzerClient.analyze(request.description());
        if (analysis.requiredSkills() == null || analysis.requiredSkills().isEmpty()) {
            throw new IllegalArgumentException(
                    "Não encontramos tecnologias na descrição. Inclua os requisitos técnicos da vaga.");
        }

        JobResponse job = jobService.create(new JobCreateRequest(
                request.title(),
                request.company(),
                request.description(),
                request.sourceUrl(),
                request.location(),
                request.workModel(),
                request.seniority(),
                request.postedAt(),
                new LinkedHashSet<>(analysis.requiredSkills()),
                new LinkedHashSet<>(analysis.desirableSkills())));
        JobMatchResponse match = matchService.analyze(email, job.id());
        return new JobImportResponse(job, match, analysis);
    }
}
