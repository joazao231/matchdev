package br.com.joao.matchdev.tracking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationUpdateRequest(
        @NotNull(message = "Informe a etapa da candidatura")
        ApplicationStatus status,

        @Size(max = 1000, message = "As anotações devem ter até 1.000 caracteres")
        String notes) {
}
