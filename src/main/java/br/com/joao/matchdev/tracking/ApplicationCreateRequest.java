package br.com.joao.matchdev.tracking;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequest(
        @NotNull(message = "Informe a vaga")
        UUID jobId,

        @NotNull(message = "Informe a etapa da candidatura")
        ApplicationStatus status,

        @Size(max = 1000, message = "As anotações devem ter até 1.000 caracteres")
        String notes) {
}
