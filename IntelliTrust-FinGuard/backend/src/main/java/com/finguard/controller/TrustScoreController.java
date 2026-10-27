package com.finguard.controller;

import com.finguard.dto.trust.TrustScoreResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.TrustScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trust-score")
@RequiredArgsConstructor
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    @GetMapping
    public TrustScoreResponse getTrustScore(@AuthenticationPrincipal AppUserPrincipal principal) {
        return trustScoreService.getStatus(principal.getUser());
    }
}
