package com.finguard.dto.location;

import java.time.Instant;

public record LocationResponse(
        double latitude,
        double longitude,
        Double accuracy,
        Instant capturedAt
) {
}
