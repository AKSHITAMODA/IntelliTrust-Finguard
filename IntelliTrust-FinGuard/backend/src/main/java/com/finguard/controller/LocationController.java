package com.finguard.controller;

import com.finguard.dto.location.LocationRequest;
import com.finguard.dto.location.LocationResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public LocationResponse record(@AuthenticationPrincipal AppUserPrincipal principal, @Valid @RequestBody LocationRequest request) {
        return locationService.recordLocation(principal.getUser(), request);
    }

    @GetMapping("/latest")
    public LocationResponse latest(@AuthenticationPrincipal AppUserPrincipal principal) {
        return locationService.latestLocationResponse(principal.getUser());
    }
}
