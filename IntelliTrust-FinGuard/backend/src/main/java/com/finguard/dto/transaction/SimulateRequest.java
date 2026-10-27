package com.finguard.dto.transaction;

public record SimulateRequest(
        Integer count,
        Boolean forceSuspicious
) {
    public int countOrDefault() {
        return count == null ? 1 : Math.min(Math.max(count, 1), 20);
    }

    public boolean forceSuspiciousOrDefault() {
        return forceSuspicious != null && forceSuspicious;
    }
}
