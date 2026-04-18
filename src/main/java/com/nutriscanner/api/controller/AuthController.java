package com.nutriscanner.api.controller;

import com.nutriscanner.api.dto.request.LoginRequest;
import com.nutriscanner.api.dto.request.RegisterRequest;
import com.nutriscanner.api.dto.response.AuthResponse;
import com.nutriscanner.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @RequestHeader("X-User-Id") String userId) {
        authService.deleteAccount(UUID.fromString(userId));
        return ResponseEntity.ok().build();
    }
}