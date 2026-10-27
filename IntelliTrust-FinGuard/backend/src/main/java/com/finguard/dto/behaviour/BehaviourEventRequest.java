package com.finguard.dto.behaviour;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BehaviourEventRequest(
        @NotNull @PositiveOrZero Double typingSpeedWpm,
        @NotNull @PositiveOrZero Double avgKeyIntervalMs,
        @NotNull @PositiveOrZero Double mouseSpeedPxS,
        @NotNull @PositiveOrZero Double clickRatePerMin,
        @NotNull @PositiveOrZero Double sessionDurationS
) {
}
