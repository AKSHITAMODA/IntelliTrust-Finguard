package com.finguard.controller;

import com.finguard.dto.fraud.FraudExplanationResponse;
import com.finguard.dto.fraud.FraudSummaryResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.FraudService;
import com.finguard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudService fraudService;
    private final TransactionService transactionService;

    @GetMapping("/explain/{transactionId}")
    public FraudExplanationResponse explain(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long transactionId) {
        var transaction = transactionService.getOwnedEntity(principal.getUser(), transactionId);
        return fraudService.explain(principal.getUser(), transaction);
    }

    @GetMapping("/summary")
    public FraudSummaryResponse summary(@AuthenticationPrincipal AppUserPrincipal principal) {
        return fraudService.summary(principal.getUser());
    }
}
