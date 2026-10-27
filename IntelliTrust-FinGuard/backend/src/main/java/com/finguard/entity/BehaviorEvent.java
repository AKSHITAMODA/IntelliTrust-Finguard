package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "behavior_events", indexes = {
        @Index(name = "idx_behavior_user_captured", columnList = "user_id, captured_at")
})
@Getter
@Setter
@NoArgsConstructor
public class BehaviorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "typing_speed_wpm", nullable = false)
    private double typingSpeedWpm;

    @Column(name = "avg_key_interval_ms", nullable = false)
    private double avgKeyIntervalMs;

    @Column(name = "mouse_speed_px_s", nullable = false)
    private double mouseSpeedPxS;

    @Column(name = "click_rate_per_min", nullable = false)
    private double clickRatePerMin;

    @Column(name = "session_duration_s", nullable = false)
    private double sessionDurationS;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Column(name = "is_anomaly", nullable = false)
    private boolean anomaly;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt = Instant.now();
}
