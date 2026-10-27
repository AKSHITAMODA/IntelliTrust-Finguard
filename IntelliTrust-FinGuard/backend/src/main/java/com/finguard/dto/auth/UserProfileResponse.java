package com.finguard.dto.auth;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        double trustScore,
        Instant createdAt
) {
}
