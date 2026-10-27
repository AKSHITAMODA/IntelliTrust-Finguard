package com.finguard.dto.forecast;

public record ForecastResponse(
        String method,
        int trainingMonths,
        int outliersFiltered,
        double rSquared,
        double forecastAmount,
        String monthLabel,
        boolean reliable
) {
}
