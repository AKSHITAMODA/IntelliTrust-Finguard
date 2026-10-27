package com.finguard.service;

import com.finguard.dto.behaviour.BehaviourEventRequest;
import com.finguard.dto.behaviour.BehaviourStatusResponse;
import com.finguard.entity.*;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.BehaviorEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Passive behavioural-biometrics monitoring: typing rhythm, mouse speed and click
 * cadence are compared to the user's own rolling median baseline. Only derived
 * timing/speed metrics are ever received or stored -- actual keystrokes are never
 * captured by this service or its callers.
 */
@Service
@RequiredArgsConstructor
public class BehaviourService {

    private static final int MIN_BASELINE_SAMPLES = 3;
    private static final double MODERATE_THRESHOLD = 30.0;
    private static final double MAJOR_THRESHOLD = 60.0;

    private final BehaviorEventRepository behaviorEventRepository;
    private final AlertRepository alertRepository;
    private final TrustScoreService trustScoreService;

    @Transactional
    public BehaviourStatusResponse recordEvent(User user, BehaviourEventRequest request) {
        List<BehaviorEvent> history = behaviorEventRepository.findTop30ByUserOrderByCapturedAtDesc(user);

        BehaviorEvent event = new BehaviorEvent();
        event.setUser(user);
        event.setTypingSpeedWpm(request.typingSpeedWpm());
        event.setAvgKeyIntervalMs(request.avgKeyIntervalMs());
        event.setMouseSpeedPxS(request.mouseSpeedPxS());
        event.setClickRatePerMin(request.clickRatePerMin());
        event.setSessionDurationS(request.sessionDurationS());

        if (history.size() < MIN_BASELINE_SAMPLES) {
            event.setRiskScore(0);
            event.setAnomaly(false);
            event.setReason("Establishing personal baseline (" + (history.size() + 1) + "/" + MIN_BASELINE_SAMPLES + " samples).");
            behaviorEventRepository.save(event);
            return statusResponse(user, history, event);
        }

        double baseTyping = median(history, BehaviorEvent::getTypingSpeedWpm);
        double baseKeyInterval = median(history, BehaviorEvent::getAvgKeyIntervalMs);
        double baseMouse = median(history, BehaviorEvent::getMouseSpeedPxS);
        double baseClick = median(history, BehaviorEvent::getClickRatePerMin);

        double typingDev = percentDeviation(request.typingSpeedWpm(), baseTyping);
        double keyIntervalDev = percentDeviation(request.avgKeyIntervalMs(), baseKeyInterval);
        double mouseDev = percentDeviation(request.mouseSpeedPxS(), baseMouse);
        double clickDev = percentDeviation(request.clickRatePerMin(), baseClick);

        double riskScore = Math.min(100.0, (typingDev + keyIntervalDev + mouseDev + clickDev) / 4.0 * 100.0);

        List<String> reasons = new ArrayList<>();
        if (typingDev > 0.3) reasons.add(String.format("Typing speed deviated %.0f%% from your baseline (%.0f vs %.0f WPM).", typingDev * 100, request.typingSpeedWpm(), baseTyping));
        if (keyIntervalDev > 0.3) reasons.add(String.format("Key-press interval deviated %.0f%% from your baseline.", keyIntervalDev * 100));
        if (mouseDev > 0.3) reasons.add(String.format("Mouse movement speed deviated %.0f%% from your baseline.", mouseDev * 100));
        if (clickDev > 0.3) reasons.add(String.format("Click rate deviated %.0f%% from your baseline.", clickDev * 100));

        boolean anomaly = riskScore >= MAJOR_THRESHOLD;
        event.setRiskScore(riskScore);
        event.setAnomaly(anomaly);
        event.setReason(reasons.isEmpty() ? "Behaviour consistent with your usual pattern." : String.join(" ", reasons));
        behaviorEventRepository.save(event);

        if (riskScore >= MODERATE_THRESHOLD) {
            Alert alert = new Alert();
            alert.setUser(user);
            alert.setAlertType(AlertType.BEHAVIOUR);
            alert.setSeverity(anomaly ? AlertSeverity.HIGH : AlertSeverity.MEDIUM);
            alert.setMessage(event.getReason());
            alertRepository.save(alert);

            double penalty = anomaly ? 12.0 : 5.0;
            trustScoreService.applyPenalty(user, penalty);
        }

        return statusResponse(user, history, event);
    }

    public BehaviourStatusResponse getStatus(User user) {
        List<BehaviorEvent> history = behaviorEventRepository.findTop30ByUserOrderByCapturedAtDesc(user);
        BehaviorEvent latest = history.isEmpty() ? null : history.get(0);
        return statusResponse(user, history.size() > 1 ? history.subList(1, history.size()) : List.of(), latest);
    }

    private BehaviourStatusResponse statusResponse(User user, List<BehaviorEvent> priorHistory, BehaviorEvent latest) {
        boolean baselineEstablished = priorHistory.size() >= MIN_BASELINE_SAMPLES;
        String status;
        double lastRisk = latest == null ? 0 : latest.getRiskScore();
        if (latest == null || !baselineEstablished) {
            status = "LEARNING";
        } else if (lastRisk >= MAJOR_THRESHOLD) {
            status = "SUSPICIOUS";
        } else if (lastRisk >= MODERATE_THRESHOLD) {
            status = "MODERATE";
        } else {
            status = "NORMAL";
        }

        List<BehaviourStatusResponse.BehaviourSample> samples = behaviorEventRepository
                .findTop30ByUserOrderByCapturedAtDesc(user).stream()
                .limit(10)
                .map(e -> new BehaviourStatusResponse.BehaviourSample(
                        e.getTypingSpeedWpm(), e.getAvgKeyIntervalMs(), e.getMouseSpeedPxS(),
                        e.getClickRatePerMin(), e.getRiskScore(), e.isAnomaly(), e.getReason(), e.getCapturedAt()
                ))
                .toList();

        return new BehaviourStatusResponse(
                status,
                lastRisk,
                baselineEstablished,
                priorHistory.size() + (latest == null ? 0 : 1),
                samples,
                latest == null ? null : latest.getCapturedAt()
        );
    }

    private double percentDeviation(double current, double baseline) {
        double denom = Math.max(Math.abs(baseline), 1.0);
        return Math.abs(current - baseline) / denom;
    }

    private double median(List<BehaviorEvent> events, java.util.function.ToDoubleFunction<BehaviorEvent> extractor) {
        double[] values = events.stream().mapToDouble(extractor).sorted().toArray();
        int n = values.length;
        if (n == 0) return 0;
        if (n % 2 == 1) return values[n / 2];
        return (values[n / 2 - 1] + values[n / 2]) / 2.0;
    }
}
