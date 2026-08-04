package br.com.joao.matchdev.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "O nome deve ter até 120 caracteres")
        String fullName,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "Informe um e-mail válido")
        String email,

        @NotBlank(message = "Informe a senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password) {
}
