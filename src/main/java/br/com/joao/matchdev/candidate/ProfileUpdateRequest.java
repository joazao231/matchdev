package br.com.joao.matchdev.candidate;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "Informe um título profissional")
        @Size(max = 180, message = "O título deve ter até 180 caracteres")
        String headline,

        @NotBlank(message = "Informe o cargo desejado")
        @Size(max = 120, message = "O cargo deve ter até 120 caracteres")
        String desiredRole,

        @NotBlank(message = "Informe sua localização")
        @Size(max = 120, message = "A localização deve ter até 120 caracteres")
        String location,

        @NotNull(message = "Informe a senioridade desejada")
        Seniority desiredSeniority,

        @NotEmpty(message = "Informe pelo menos uma habilidade")
        Set<@NotBlank(message = "A habilidade não pode estar vazia")
                @Size(max = 80, message = "Cada habilidade deve ter até 80 caracteres") String> skills,

        @NotEmpty(message = "Informe pelo menos um modelo de trabalho")
        Set<WorkModel> preferredWorkModels) {
}
