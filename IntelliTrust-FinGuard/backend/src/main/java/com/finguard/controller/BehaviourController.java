package com.finguard.controller;

import com.finguard.dto.behaviour.BehaviourEventRequest;
import com.finguard.dto.behaviour.BehaviourStatusResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.BehaviourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/behaviour")
@RequiredArgsConstructor
public class BehaviourController {

    private final BehaviourService behaviourService;

    @PostMapping("/event")
    public BehaviourStatusResponse recordEvent(@AuthenticationPrincipal AppUserPrincipal principal, @Valid @RequestBody BehaviourEventRequest request) {
        return behaviourService.recordEvent(principal.getUser(), request);
    }

    @GetMapping("/status")
    public BehaviourStatusResponse status(@AuthenticationPrincipal AppUserPrincipal principal) {
        return behaviourService.getStatus(principal.getUser());
    }
}
