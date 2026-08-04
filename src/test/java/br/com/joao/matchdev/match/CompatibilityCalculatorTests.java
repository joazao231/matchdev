package br.com.joao.matchdev.match;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import br.com.joao.matchdev.auth.UserAccount;
import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import br.com.joao.matchdev.job.JobPosting;
import org.junit.jupiter.api.Test;

class CompatibilityCalculatorTests {

    private final CompatibilityCalculator calculator = new CompatibilityCalculator();

    @Test
    void shouldReturnExcellentMatchWhenProfileMeetsTheMainRequirements() {
        CandidateProfile profile = profile(
                "Desenvolvedor Backend",
                Seniority.JUNIOR,
                Set.of("java", "spring boot", "postgresql", "docker"),
                Set.of(WorkModel.REMOTE));
        JobPosting job = job(
                "Desenvolvedor Backend Java Júnior",
                Seniority.JUNIOR,
                WorkModel.REMOTE,
                Set.of("java", "spring boot", "postgresql"),
                Set.of("docker", "aws"));

        CompatibilityResult result = calculator.calculate(profile, job);

        assertThat(result.score()).isEqualTo(93);
        assertThat(result.recommendation()).isEqualTo(MatchRecommendation.EXCELLENT);
        assertThat(result.missingRequiredSkills()).isEmpty();
        assertThat(result.matchedSkills()).contains("java", "spring boot", "postgresql", "docker");
    }

    @Test
    void shouldExplainMissingRequiredSkillsForALowMatch() {
        CandidateProfile profile = profile(
                "Desenvolvedor Backend",
                Seniority.JUNIOR,
                Set.of("java"),
                Set.of(WorkModel.REMOTE));
        JobPosting job = job(
                "Engenheiro de Dados",
                Seniority.SENIOR,
                WorkModel.ONSITE,
                Set.of("python", "spark", "aws"),
                Set.of("kafka"));

        CompatibilityResult result = calculator.calculate(profile, job);

        assertThat(result.score()).isZero();
        assertThat(result.recommendation()).isEqualTo(MatchRecommendation.LOW);
        assertThat(result.missingRequiredSkills()).containsExactlyInAnyOrder("python", "spark", "aws");
        assertThat(result.explanation()).contains("Principais lacunas");
    }

    private CandidateProfile profile(
            String desiredRole,
            Seniority seniority,
            Set<String> skills,
            Set<WorkModel> workModels) {
        UserAccount user = new UserAccount("João", "joao@example.com", "hash");
        CandidateProfile profile = new CandidateProfile(user);
        profile.update(
                "Backend Developer",
                desiredRole,
                "Umuarama - PR",
                seniority,
                skills,
                workModels);
        return profile;
    }

    private JobPosting job(
            String title,
            Seniority seniority,
            WorkModel workModel,
            Set<String> required,
            Set<String> desirable) {
        return new JobPosting(
                title,
                "Empresa",
                "Descrição da vaga",
                null,
                "Brasil",
                workModel,
                seniority,
                LocalDate.now(),
                required,
                desirable);
    }
}
