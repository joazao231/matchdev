package br.com.joao.matchdev.config;

import java.time.LocalDate;
import java.util.Set;

import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import br.com.joao.matchdev.job.JobPosting;
import br.com.joao.matchdev.job.JobPostingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DemoDataConfig {

    @Bean
    CommandLineRunner demoJobs(JobPostingRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(new JobPosting(
                    "Desenvolvedor Backend Java Júnior",
                    "TechNova",
                    "Desenvolvimento de APIs REST com Java, Spring Boot e PostgreSQL.",
                    null,
                    "Remoto - Brasil",
                    WorkModel.REMOTE,
                    Seniority.JUNIOR,
                    LocalDate.now().minusDays(1),
                    Set.of("java", "spring boot", "api rest", "postgresql"),
                    Set.of("docker", "github actions")));

            repository.save(new JobPosting(
                    "Pessoa Desenvolvedora Python Júnior",
                    "CodeLab",
                    "Construção e manutenção de serviços Python com FastAPI.",
                    null,
                    "Curitiba - PR",
                    WorkModel.HYBRID,
                    Seniority.JUNIOR,
                    LocalDate.now().minusDays(2),
                    Set.of("python", "fastapi", "sql"),
                    Set.of("docker", "pytest")));

            repository.save(new JobPosting(
                    "Backend Developer Pleno",
                    "DataWave",
                    "Serviços distribuídos, mensageria e observabilidade em ambiente de nuvem.",
                    null,
                    "São Paulo - SP",
                    WorkModel.REMOTE,
                    Seniority.MID_LEVEL,
                    LocalDate.now().minusDays(3),
                    Set.of("java", "microservices", "kafka", "aws"),
                    Set.of("kubernetes", "prometheus")));
        };
    }
}
