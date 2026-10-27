package com.finguard.dto.location;

import jakarta.validation.constraints.NotNull;

public record LocationRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double accuracy
) {
}
