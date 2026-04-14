package com.nutriscanner.api.service;

import com.nutriscanner.api.dto.request.LoginRequest;
import com.nutriscanner.api.dto.request.RegisterRequest;
import com.nutriscanner.api.dto.response.AuthResponse;
import com.nutriscanner.api.model.User;
import com.nutriscanner.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(
                saved.getId().toString(),
                saved.getRole()
        );

        return new AuthResponse(
                token,
                saved.getId().toString(),
                saved.getEmail(),
                saved.getFullName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("INVALID_CREDENTIALS"));

        if (!user.getIsActive()) {
            throw new RuntimeException("ACCOUNT_DISABLED");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        String token = jwtService.generateToken(
                user.getId().toString(),
                user.getRole()
        );

        return new AuthResponse(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getFullName()
        );
    }
}