package com.finguard.controller;

import com.finguard.dto.forecast.ForecastResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    public ForecastResponse forecast(@AuthenticationPrincipal AppUserPrincipal principal) {
        return forecastService.forecast(principal.getUser());
    }
}
