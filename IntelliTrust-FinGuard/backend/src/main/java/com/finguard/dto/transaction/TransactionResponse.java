package com.finguard.dto.transaction;

import com.finguard.entity.Transaction;
import com.finguard.entity.TransactionCategory;
import com.finguard.entity.TransactionSource;
import com.finguard.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        LocalDateTime txnDate,
        String description,
        String merchant,
        TransactionCategory category,
        BigDecimal amount,
        TransactionType txnType,
        TransactionSource source,
        Double latitude,
        Double longitude,
        String city,
        String country,
        double riskScore,
        boolean fraud,
        String fraudReason
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getTxnDate(), t.getDescription(), t.getMerchant(), t.getCategory(),
                t.getAmount(), t.getTxnType(), t.getSource(), t.getLatitude(), t.getLongitude(),
                t.getCity(), t.getCountry(), t.getRiskScore(), t.isFraud(), t.getFraudReason()
        );
    }
}
