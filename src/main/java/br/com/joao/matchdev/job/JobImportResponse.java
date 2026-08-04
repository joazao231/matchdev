package br.com.joao.matchdev.job;

import br.com.joao.matchdev.integration.job.JobAnalysisResponse;
import br.com.joao.matchdev.match.JobMatchResponse;

public record JobImportResponse(
        JobResponse job,
        JobMatchResponse match,
        JobAnalysisResponse analysis) {
}
