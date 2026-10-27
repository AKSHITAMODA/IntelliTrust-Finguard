package com.finguard.service;

import com.finguard.dto.forecast.ForecastResponse;
import com.finguard.entity.Transaction;
import com.finguard.entity.TransactionType;
import com.finguard.entity.User;
import com.finguard.ml.SimpleLinearRegression;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Forecasts next month's spending with ordinary least-squares linear regression
 * over completed historical months, reporting R^2 and training size so the
 * prediction's reliability is transparent rather than a black box.
 */
@Service
@RequiredArgsConstructor
public class ForecastService {

    private static final int MIN_MONTHS = 3;

    private final TransactionRepository transactionRepository;

    public ForecastResponse forecast(User user) {
        List<Transaction> debits = transactionRepository.findByUserOrderByTxnDateDesc(user).stream()
                .filter(t -> t.getTxnType() == TransactionType.DEBIT)
                .toList();

        YearMonth currentMonth = YearMonth.now();
        Map<YearMonth, Double> monthlyTotals = new TreeMap<>();
        for (Transaction t : debits) {
            YearMonth ym = YearMonth.from(t.getTxnDate());
            if (ym.equals(currentMonth)) continue; // only completed months are used for training
            monthlyTotals.merge(ym, t.getAmount().doubleValue(), Double::sum);
        }

        if (monthlyTotals.size() < MIN_MONTHS) {
            return new ForecastResponse("linear_regression", monthlyTotals.size(), 0, 0.0, 0.0,
                    currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear(),
                    false);
        }

        List<YearMonth> months = new ArrayList<>(monthlyTotals.keySet());
        double[] rawValues = months.stream().mapToDouble(monthlyTotals::get).toArray();

        double mean = Arrays.stream(rawValues).average().orElse(0);
        double std = Math.sqrt(Arrays.stream(rawValues).map(v -> (v - mean) * (v - mean)).average().orElse(0));

        List<Double> filteredX = new ArrayList<>();
        List<Double> filteredY = new ArrayList<>();
        int outliers = 0;
        for (int i = 0; i < months.size(); i++) {
            double value = rawValues[i];
            if (std > 0 && Math.abs(value - mean) > 2.5 * std) {
                outliers++;
                continue;
            }
            filteredX.add((double) i);
            filteredY.add(value);
        }
        if (filteredX.size() < MIN_MONTHS) {
            // Not enough points survive filtering; fall back to the unfiltered series.
            filteredX.clear();
            filteredY.clear();
            for (int i = 0; i < months.size(); i++) {
                filteredX.add((double) i);
                filteredY.add(rawValues[i]);
            }
            outliers = 0;
        }

        double[] x = filteredX.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = filteredY.stream().mapToDouble(Double::doubleValue).toArray();

        SimpleLinearRegression regression = new SimpleLinearRegression();
        regression.fit(x, y);

        double nextOrdinal = months.size();
        double forecastAmount = Math.max(0, regression.predict(nextOrdinal));

        YearMonth forecastMonth = currentMonth;
        String label = forecastMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + forecastMonth.getYear();

        return new ForecastResponse("linear_regression", months.size(), outliers,
                Math.round(regression.getRSquared() * 1000.0) / 1000.0,
                Math.round(forecastAmount * 100.0) / 100.0,
                label,
                regression.getRSquared() >= 0.3);
    }
}
