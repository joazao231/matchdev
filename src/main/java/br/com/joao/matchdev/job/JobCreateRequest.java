package br.com.joao.matchdev.job;

import java.time.LocalDate;
import java.util.Set;

import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobCreateRequest(
        @NotBlank(message = "Informe o título da vaga")
        @Size(max = 160, message = "O título deve ter até 160 caracteres")
        String title,

        @NotBlank(message = "Informe a empresa")
        @Size(max = 120, message = "A empresa deve ter até 120 caracteres")
        String company,

        @NotBlank(message = "Informe a descrição")
        @Size(max = 10000, message = "A descrição deve ter até 10.000 caracteres")
        String description,

        @Size(max = 600, message = "A URL deve ter até 600 caracteres")
        String sourceUrl,

        @NotBlank(message = "Informe a localização")
        @Size(max = 120, message = "A localização deve ter até 120 caracteres")
        String location,

        @NotNull(message = "Informe o modelo de trabalho")
        WorkModel workModel,

        @NotNull(message = "Informe a senioridade")
        Seniority seniority,

        LocalDate postedAt,

        @NotEmpty(message = "Informe pelo menos uma habilidade obrigatória")
        Set<@NotBlank @Size(max = 80) String> requiredSkills,

        Set<@NotBlank @Size(max = 80) String> desirableSkills) {
}
