package com.finguard.dto.behaviour;

import java.time.Instant;
import java.util.List;

public record BehaviourStatusResponse(
        String status,
        double lastRiskScore,
        boolean baselineEstablished,
        int sampleCount,
        List<BehaviourSample> recentSamples,
        Instant lastCapturedAt
) {
    public record BehaviourSample(
            double typingSpeedWpm,
            double avgKeyIntervalMs,
            double mouseSpeedPxS,
            double clickRatePerMin,
            double riskScore,
            boolean anomaly,
            String reason,
            Instant capturedAt
    ) {
    }
}
