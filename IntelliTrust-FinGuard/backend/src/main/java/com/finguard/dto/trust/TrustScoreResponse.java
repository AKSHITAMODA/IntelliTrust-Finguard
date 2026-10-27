package com.finguard.dto.trust;

public record TrustScoreResponse(
        double trustScore,
        String status,
        String explanation
) {
}
