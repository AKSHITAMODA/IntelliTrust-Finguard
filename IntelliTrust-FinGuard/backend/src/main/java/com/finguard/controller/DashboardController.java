package com.finguard.controller;

import com.finguard.dto.dashboard.DashboardResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.getDashboard(principal.getUser());
    }
}
