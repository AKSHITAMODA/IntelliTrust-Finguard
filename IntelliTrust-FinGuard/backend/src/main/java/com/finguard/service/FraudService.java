package com.finguard.service;

import com.finguard.dto.fraud.FraudExplanationResponse;
import com.finguard.dto.fraud.FraudSummaryResponse;
import com.finguard.dto.transaction.TransactionResponse;
import com.finguard.entity.*;
import com.finguard.ml.IsolationForestLite;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.BehaviorEventRepository;
import com.finguard.repository.TransactionRepository;
import com.finguard.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hybrid fraud-detection pipeline combining five explainable signals into a single
 * 0-100 risk score: amount deviation, transaction frequency, an unsupervised
 * Isolation Forest anomaly score, geographic (Haversine) distance risk and the
 * user's latest behavioural-biometric risk.
 */
@Service
@RequiredArgsConstructor
public class FraudService {

    private static final double FRAUD_THRESHOLD = 55.0;
    private static final int MIN_HISTORY_FOR_ANOMALY = 5;

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final BehaviorEventRepository behaviorEventRepository;
    private final LocationService locationService;
    private final TrustScoreService trustScoreService;

    private record Breakdown(double amountRisk, double frequencyRisk, double anomalyScore, double geoRisk,
                              double behaviouralRisk, double riskScore, boolean fraud, List<String> reasons) {}

    @Transactional
    public FraudExplanationResponse evaluate(User user, Transaction transaction) {
        if (transaction.getTxnType() != TransactionType.DEBIT) {
            transaction.setRiskScore(0);
            transaction.setFraud(false);
            transaction.setFraudReason(null);
            transactionRepository.save(transaction);
            return new FraudExplanationResponse(transaction.getId(), 0, false, 0, 0, 0, 0, 0, List.of("Credit transactions are not risk-scored."));
        }

        Breakdown b = computeBreakdown(user, transaction);

        transaction.setRiskScore(b.riskScore());
        transaction.setFraud(b.fraud());
        transaction.setFraudReason(String.join(" ", b.reasons()));
        transactionRepository.save(transaction);

        if (b.fraud()) {
            Alert alert = new Alert();
            alert.setUser(user);
            alert.setTransaction(transaction);
            alert.setAlertType(AlertType.FRAUD);
            alert.setSeverity(b.riskScore() >= 85 ? AlertSeverity.CRITICAL : b.riskScore() >= 75 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM);
            alert.setMessage(transaction.getFraudReason());
            alertRepository.save(alert);

            trustScoreService.applyPenalty(user, b.riskScore() / 5.0);
        }

        return toResponse(transaction.getId(), b);
    }

    /** Read-only recomputation of the risk breakdown for the explainability panel; does not persist or re-alert. */
    public FraudExplanationResponse explain(User user, Transaction transaction) {
        if (transaction.getTxnType() != TransactionType.DEBIT) {
            return new FraudExplanationResponse(transaction.getId(), 0, false, 0, 0, 0, 0, 0, List.of("Credit transactions are not risk-scored."));
        }
        Breakdown b = computeBreakdown(user, transaction);
        return toResponse(transaction.getId(), b);
    }

    private FraudExplanationResponse toResponse(Long transactionId, Breakdown b) {
        return new FraudExplanationResponse(transactionId, round(b.riskScore()), b.fraud(),
                round(b.amountRisk()), round(b.frequencyRisk()), round(b.anomalyScore()), round(b.geoRisk()), round(b.behaviouralRisk()), b.reasons());
    }

    private Breakdown computeBreakdown(User user, Transaction transaction) {
        List<Transaction> history = transactionRepository
                .findTop120ByUserAndTxnTypeOrderByTxnDateDesc(user, TransactionType.DEBIT)
                .stream()
                .filter(t -> !t.getId().equals(transaction.getId()))
                .toList();

        double amountRisk = computeAmountRisk(transaction, history);
        double frequencyRisk = computeFrequencyRisk(user, transaction);
        double anomalyScore = computeAnomalyScore(transaction, history);
        double geoRisk = computeGeoRisk(user, transaction);
        double behaviouralRisk = computeBehaviouralRisk(user);

        double combined = amountRisk * 0.30
                + frequencyRisk * 0.10
                + anomalyScore * 0.25
                + geoRisk * 0.25
                + behaviouralRisk * 0.10;
        double riskScore = Math.min(100.0, Math.max(0.0, combined));
        boolean isFraud = riskScore >= FRAUD_THRESHOLD;

        List<String> reasons = buildReasons(transaction, history, amountRisk, frequencyRisk, anomalyScore, geoRisk, behaviouralRisk);
        return new Breakdown(amountRisk, frequencyRisk, anomalyScore, geoRisk, behaviouralRisk, riskScore, isFraud, reasons);
    }

    private double computeAmountRisk(Transaction transaction, List<Transaction> history) {
        double amount = transaction.getAmount().doubleValue();
        double median = median(history.stream().map(t -> t.getAmount().doubleValue()).sorted().toArray(Double[]::new));

        double risk = 0;
        if (median > 0 && amount > median) {
            double ratio = amount / median;
            risk = Math.min(100.0, (ratio - 1.0) * 45.0);
        }
        if (amount > 50000) risk = Math.max(risk, 55);
        if (amount > 100000) risk = Math.max(risk, 85);
        return Math.min(100, risk);
    }

    private double computeFrequencyRisk(User user, Transaction transaction) {
        LocalDateTime windowStart = transaction.getTxnDate().minusHours(1);
        long count = transactionRepository
                .findByUserAndTxnDateBetween(user, windowStart, transaction.getTxnDate())
                .stream()
                .filter(t -> !t.getId().equals(transaction.getId()))
                .count();

        if (count >= 5) return 90;
        if (count >= 3) return 60;
        if (count >= 2) return 30;
        return 0;
    }

    private double computeAnomalyScore(Transaction transaction, List<Transaction> history) {
        if (history.size() < MIN_HISTORY_FOR_ANOMALY) return 0;

        double[][] featureMatrix = new double[history.size()][];
        for (int i = 0; i < history.size(); i++) {
            featureMatrix[i] = featuresFor(history.get(i));
        }

        IsolationForestLite forest = new IsolationForestLite(100, Math.min(256, featureMatrix.length), 42L);
        forest.fit(featureMatrix);

        double score = forest.score(featuresFor(transaction));
        double scaled = (score - 0.45) / 0.35 * 100.0;
        return Math.min(100, Math.max(0, scaled));
    }

    private double[] featuresFor(Transaction t) {
        double amount = t.getAmount().doubleValue();
        int hour = t.getTxnDate().getHour();
        double hourSin = Math.sin(2 * Math.PI * hour / 24.0);
        double hourCos = Math.cos(2 * Math.PI * hour / 24.0);
        return new double[]{amount, hourSin, hourCos};
    }

    private double computeGeoRisk(User user, Transaction transaction) {
        if (transaction.getLatitude() == null || transaction.getLongitude() == null) return 0;
        var latest = locationService.latestLocation(user);
        if (latest.isEmpty()) return 0;

        double distanceKm = GeoUtils.haversineKm(
                latest.get().getLatitude(), latest.get().getLongitude(),
                transaction.getLatitude(), transaction.getLongitude());

        if (distanceKm > 1000) return 90;
        if (distanceKm > 500) return 60;
        return Math.min(30, distanceKm / 500.0 * 30);
    }

    private double computeBehaviouralRisk(User user) {
        return behaviorEventRepository.findByUserOrderByCapturedAtDesc(user).stream()
                .findFirst()
                .map(BehaviorEvent::getRiskScore)
                .orElse(0.0);
    }

    private List<String> buildReasons(Transaction transaction, List<Transaction> history,
                                       double amountRisk, double frequencyRisk, double anomalyScore,
                                       double geoRisk, double behaviouralRisk) {
        List<String> reasons = new ArrayList<>();
        double median = median(history.stream().map(t -> t.getAmount().doubleValue()).sorted().toArray(Double[]::new));

        if (amountRisk >= 50) {
            reasons.add(String.format("Amount ₹%.2f is well above your recent median spending of ₹%.2f.", transaction.getAmount().doubleValue(), median));
        }
        if (frequencyRisk >= 30) {
            reasons.add("Unusually high transaction frequency detected in the last hour.");
        }
        if (anomalyScore >= 50) {
            reasons.add(String.format("Isolation Forest flagged this transaction as a statistical anomaly (score %.0f/100).", anomalyScore));
        }
        if (geoRisk >= 50) {
            reasons.add("Transaction location is far from your last known live location.");
        }
        if (behaviouralRisk >= 30) {
            reasons.add("Recent interaction behaviour deviates from your typical pattern.");
        }
        if (reasons.isEmpty()) {
            reasons.add("No significant risk factors detected; transaction is consistent with your history.");
        }
        return reasons;
    }

    private double median(Double[] sortedValues) {
        int n = sortedValues.length;
        if (n == 0) return 0;
        if (n % 2 == 1) return sortedValues[n / 2];
        return (sortedValues[n / 2 - 1] + sortedValues[n / 2]) / 2.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public FraudSummaryResponse summary(User user) {
        List<Transaction> flagged = transactionRepository.findByUserOrderByTxnDateDesc(user).stream()
                .filter(Transaction::isFraud)
                .toList();
        double avg = flagged.stream().mapToDouble(Transaction::getRiskScore).average().orElse(0);
        List<TransactionResponse> recent = flagged.stream().limit(10).map(TransactionResponse::from).toList();
        return new FraudSummaryResponse(flagged.size(), round(avg), recent);
    }
}
