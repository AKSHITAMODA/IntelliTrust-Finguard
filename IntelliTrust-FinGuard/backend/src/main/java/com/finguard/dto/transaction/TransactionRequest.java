package com.finguard.dto.transaction;

import com.finguard.entity.TransactionCategory;
import com.finguard.entity.TransactionType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        @NotNull LocalDateTime txnDate,
        String description,
        String merchant,
        TransactionCategory category,
        @NotNull BigDecimal amount,
        @NotNull TransactionType txnType,
        Double latitude,
        Double longitude,
        String city,
        String country
) {
}
