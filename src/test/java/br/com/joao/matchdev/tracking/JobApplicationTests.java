package br.com.joao.matchdev.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import br.com.joao.matchdev.auth.UserAccount;
import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import br.com.joao.matchdev.job.JobPosting;
import org.junit.jupiter.api.Test;

class JobApplicationTests {

    @Test
    void shouldRegisterWhenTheCandidateActuallyApplies() {
        JobApplication application = new JobApplication(
                new UserAccount("João", "joao@example.com", "hash"),
                new JobPosting(
                        "Backend Java",
                        "Nexa",
                        "Vaga Java Spring Boot",
                        null,
                        "Remoto",
                        WorkModel.REMOTE,
                        Seniority.JUNIOR,
                        LocalDate.now(),
                        Set.of("java", "spring boot"),
                        Set.of("docker")),
                ApplicationStatus.INTERESTED,
                "Revisar antes de enviar");

        assertThat(application.getAppliedAt()).isNull();

        application.update(ApplicationStatus.APPLIED, "Currículo enviado");

        assertThat(application.getAppliedAt()).isNotNull();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(application.getNotes()).isEqualTo("Currículo enviado");
    }
}
