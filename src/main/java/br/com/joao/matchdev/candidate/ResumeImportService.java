package br.com.joao.matchdev.candidate;

import java.util.LinkedHashSet;

import br.com.joao.matchdev.integration.resume.ResumeAnalysisResponse;
import br.com.joao.matchdev.integration.resume.ResumeParserClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeImportService {

    private final ResumeParserClient resumeParserClient;
    private final CandidateProfileService profileService;

    public ResumeImportService(
            ResumeParserClient resumeParserClient,
            CandidateProfileService profileService) {
        this.resumeParserClient = resumeParserClient;
        this.profileService = profileService;
    }

    public ResumeImportResponse importResume(String email, MultipartFile file) {
        ResumeAnalysisResponse analysis = resumeParserClient.analyze(file);
        CandidateProfileService.ResumeProfileUpdate update = profileService.mergeResumeSkills(
                email,
                new LinkedHashSet<>(analysis.skills()),
                analysis.suggestedRole());

        return new ResumeImportResponse(analysis, update.profile(), update.importedSkills());
    }
}
