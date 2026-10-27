package com.finguard.dto.fraud;

import java.util.List;

public record FraudExplanationResponse(
        Long transactionId,
        double riskScore,
        boolean fraud,
        double amountRisk,
        double frequencyRisk,
        double anomalyScore,
        double geoRisk,
        double behaviouralRisk,
        List<String> reasons
) {
}
