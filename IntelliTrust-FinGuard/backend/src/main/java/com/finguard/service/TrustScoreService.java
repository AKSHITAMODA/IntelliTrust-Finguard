package com.finguard.service;

import com.finguard.dto.trust.TrustScoreResponse;
import com.finguard.entity.User;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TrustScoreService {

    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;
    /** Passive recovery rate while no anomalies are detected. */
    private static final double RECOVERY_PER_HOUR = 0.75;

    private final UserRepository userRepository;

    @Transactional
    public void applyPenalty(User user, double penalty) {
        recoverIfDue(user);
        double updated = Math.max(MIN_SCORE, user.getTrustScore() - penalty);
        user.setTrustScore(updated);
        user.setLastTrustUpdate(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public void recoverIfDue(User user) {
        Instant last = user.getLastTrustUpdate() == null ? user.getCreatedAt() : user.getLastTrustUpdate();
        if (last == null) last = Instant.now();

        double hoursElapsed = Duration.between(last, Instant.now()).toMinutes() / 60.0;
        if (hoursElapsed <= 0 || user.getTrustScore() >= MAX_SCORE) {
            return;
        }
        double recovered = Math.min(MAX_SCORE, user.getTrustScore() + hoursElapsed * RECOVERY_PER_HOUR);
        user.setTrustScore(recovered);
        user.setLastTrustUpdate(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public TrustScoreResponse getStatus(User user) {
        recoverIfDue(user);
        double score = user.getTrustScore();
        String status;
        String explanation;
        if (score >= 85) {
            status = "EXCELLENT";
            explanation = "No significant fraud or behavioural anomalies detected recently.";
        } else if (score >= 60) {
            status = "MODERATE";
            explanation = "Some risk signals have been observed; monitor recent alerts.";
        } else {
            status = "AT_RISK";
            explanation = "Multiple fraud or behavioural anomalies have reduced your trust score.";
        }
        return new TrustScoreResponse(round(score), status, explanation);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
