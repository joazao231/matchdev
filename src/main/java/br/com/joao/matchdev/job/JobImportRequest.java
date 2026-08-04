package br.com.joao.matchdev.job;

import java.time.LocalDate;

import br.com.joao.matchdev.candidate.Seniority;
import br.com.joao.matchdev.candidate.WorkModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobImportRequest(
        @NotBlank(message = "Informe o título da vaga")
        @Size(max = 160, message = "O título deve ter até 160 caracteres")
        String title,

        @NotBlank(message = "Informe a empresa")
        @Size(max = 120, message = "A empresa deve ter até 120 caracteres")
        String company,

        @NotBlank(message = "Cole a descrição da vaga")
        @Size(min = 30, max = 10000, message = "A descrição deve ter entre 30 e 10.000 caracteres")
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

        LocalDate postedAt) {
}
