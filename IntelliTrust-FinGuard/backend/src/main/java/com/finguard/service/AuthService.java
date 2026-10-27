package com.finguard.service;

import com.finguard.dto.auth.AuthResponse;
import com.finguard.dto.auth.LoginRequest;
import com.finguard.dto.auth.RegisterRequest;
import com.finguard.entity.User;
import com.finguard.exception.AuthException;
import com.finguard.exception.BadRequestException;
import com.finguard.repository.UserRepository;
import com.finguard.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setTrustScore(100.0);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getTrustScore());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getTrustScore());
    }
}
