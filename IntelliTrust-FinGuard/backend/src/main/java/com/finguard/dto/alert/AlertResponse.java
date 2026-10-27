package com.finguard.dto.alert;

import com.finguard.entity.Alert;
import com.finguard.entity.AlertSeverity;
import com.finguard.entity.AlertType;

import java.time.Instant;

public record AlertResponse(
        Long id,
        AlertType alertType,
        AlertSeverity severity,
        String message,
        Long transactionId,
        Instant createdAt,
        boolean resolved
) {
    public static AlertResponse from(Alert a) {
        return new AlertResponse(
                a.getId(), a.getAlertType(), a.getSeverity(), a.getMessage(),
                a.getTransaction() == null ? null : a.getTransaction().getId(),
                a.getCreatedAt(), a.isResolved()
        );
    }
}
