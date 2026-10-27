package com.finguard.dto.dashboard;

import com.finguard.dto.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal netBalance,
        BigDecimal monthlySpending,
        double forecastAmount,
        String forecastMonthLabel,
        double trustScore,
        String behaviourStatus,
        long activeAlerts,
        List<HistoricalPoint> historicalSpending,
        List<CategorySlice> categoryBreakdown,
        List<TransactionResponse> recentTransactions
) {
    public record HistoricalPoint(String month, BigDecimal total) {
    }

    public record CategorySlice(String category, BigDecimal total) {
    }
}
