package com.finguard.service;

import com.finguard.dto.dashboard.DashboardResponse;
import com.finguard.dto.forecast.ForecastResponse;
import com.finguard.dto.transaction.TransactionResponse;
import com.finguard.entity.Transaction;
import com.finguard.entity.TransactionType;
import com.finguard.entity.User;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final ForecastService forecastService;
    private final TrustScoreService trustScoreService;
    private final BehaviourService behaviourService;
    private final AlertService alertService;

    public DashboardResponse getDashboard(User user) {
        List<Transaction> all = transactionRepository.findByUserOrderByTxnDateDesc(user);

        BigDecimal netBalance = BigDecimal.ZERO;
        for (Transaction t : all) {
            netBalance = t.getTxnType() == TransactionType.CREDIT
                    ? netBalance.add(t.getAmount())
                    : netBalance.subtract(t.getAmount());
        }

        Map<YearMonth, BigDecimal> monthlyDebitTotals = new TreeMap<>();
        for (Transaction t : all) {
            if (t.getTxnType() != TransactionType.DEBIT) continue;
            YearMonth ym = YearMonth.from(t.getTxnDate());
            monthlyDebitTotals.merge(ym, t.getAmount(), BigDecimal::add);
        }

        YearMonth reportingMonth = monthlyDebitTotals.isEmpty() ? YearMonth.now() : Collections.max(monthlyDebitTotals.keySet());
        BigDecimal monthlySpending = monthlyDebitTotals.getOrDefault(reportingMonth, BigDecimal.ZERO);

        List<DashboardResponse.HistoricalPoint> historical = new ArrayList<>();
        List<YearMonth> lastSixMonths = monthlyDebitTotals.keySet().stream().sorted().toList();
        int start = Math.max(0, lastSixMonths.size() - 6);
        for (YearMonth ym : lastSixMonths.subList(start, lastSixMonths.size())) {
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            historical.add(new DashboardResponse.HistoricalPoint(label, monthlyDebitTotals.get(ym).setScale(2, RoundingMode.HALF_UP)));
        }

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (Transaction t : all) {
            if (t.getTxnType() != TransactionType.DEBIT) continue;
            if (!YearMonth.from(t.getTxnDate()).equals(reportingMonth)) continue;
            categoryTotals.merge(t.getCategory().name(), t.getAmount(), BigDecimal::add);
        }
        List<DashboardResponse.CategorySlice> categoryBreakdown = categoryTotals.entrySet().stream()
                .map(e -> new DashboardResponse.CategorySlice(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();

        List<TransactionResponse> recent = transactionRepository.findTop10ByUserOrderByTxnDateDesc(user).stream()
                .map(TransactionResponse::from)
                .toList();

        ForecastResponse forecast = forecastService.forecast(user);
        double trustScore = trustScoreService.getStatus(user).trustScore();
        String behaviourStatus = behaviourService.getStatus(user).status();
        long activeAlerts = alertService.countActive(user);

        return new DashboardResponse(
                netBalance.setScale(2, RoundingMode.HALF_UP),
                monthlySpending.setScale(2, RoundingMode.HALF_UP),
                forecast.forecastAmount(),
                forecast.monthLabel(),
                trustScore,
                behaviourStatus,
                activeAlerts,
                historical,
                categoryBreakdown,
                recent
        );
    }
}
