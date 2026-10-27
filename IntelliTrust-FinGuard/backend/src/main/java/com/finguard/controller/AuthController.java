package com.finguard.controller;

import com.finguard.dto.auth.AuthResponse;
import com.finguard.dto.auth.LoginRequest;
import com.finguard.dto.auth.RegisterRequest;
import com.finguard.dto.auth.UserProfileResponse;
import com.finguard.security.AppUserPrincipal;
import com.finguard.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        var user = principal.getUser();
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getTrustScore(), user.getCreatedAt());
    }
}
