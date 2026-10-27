package com.finguard.dto.fraud;

import com.finguard.dto.transaction.TransactionResponse;

import java.util.List;

public record FraudSummaryResponse(
        long totalFlaggedTransactions,
        double averageRiskScore,
        List<TransactionResponse> recentFlagged
) {
}
