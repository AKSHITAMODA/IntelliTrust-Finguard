package com.finguard.controller;

import com.finguard.dto.alert.AlertResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public List<AlertResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return alertService.list(principal.getUser());
    }

    @PutMapping("/{id}/resolve")
    public AlertResponse resolve(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return alertService.resolve(principal.getUser(), id);
    }
}
