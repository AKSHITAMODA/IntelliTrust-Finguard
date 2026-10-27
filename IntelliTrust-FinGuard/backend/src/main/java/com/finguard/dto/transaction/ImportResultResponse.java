package com.finguard.dto.transaction;

import java.util.List;

public record ImportResultResponse(
        int totalRows,
        int imported,
        int duplicatesSkipped,
        int invalidSkipped,
        List<String> errors
) {
}
