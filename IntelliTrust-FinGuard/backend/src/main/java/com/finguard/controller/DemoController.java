package com.finguard.controller;

import com.finguard.security.AppUserPrincipal;
import com.finguard.service.DemoSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoSeedService demoSeedService;

    @PostMapping("/reset")
    public Map<String, String> reset(@AuthenticationPrincipal AppUserPrincipal principal) {
        demoSeedService.resetUser(principal.getUser());
        return Map.of("status", "reset", "message", "Demo data has been restored to the clean baseline state.");
    }
}
