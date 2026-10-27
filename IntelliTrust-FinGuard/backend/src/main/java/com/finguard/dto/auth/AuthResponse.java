package com.finguard.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        double trustScore
) {
    public static AuthResponse of(String token, Long userId, String name, String email, double trustScore) {
        return new AuthResponse(token, "Bearer", userId, name, email, trustScore);
    }
}
