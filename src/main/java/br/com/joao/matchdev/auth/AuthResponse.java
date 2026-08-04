package br.com.joao.matchdev.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String fullName,
        String email) {
}
