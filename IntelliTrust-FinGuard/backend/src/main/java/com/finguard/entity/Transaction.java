package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_user_id", columnList = "user_id"),
        @Index(name = "idx_txn_user_date", columnList = "user_id, txn_date"),
        @Index(name = "idx_txn_fingerprint", columnList = "user_id, fingerprint")
})
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "txn_date", nullable = false)
    private LocalDateTime txnDate;

    @Column(length = 255)
    private String description;

    @Column(length = 150)
    private String merchant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionCategory category = TransactionCategory.OTHER;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 10)
    private TransactionType txnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionSource source = TransactionSource.MANUAL;

    private Double latitude;
    private Double longitude;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "risk_score", nullable = false)
    private double riskScore = 0.0;

    @Column(name = "is_fraud", nullable = false)
    private boolean fraud = false;

    @Column(name = "fraud_reason", columnDefinition = "TEXT")
    private String fraudReason;

    @Column(name = "fingerprint", length = 128)
    private String fingerprint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
