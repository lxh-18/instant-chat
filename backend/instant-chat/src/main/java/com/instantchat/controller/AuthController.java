package com.instantchat.controller;

import com.instantchat.dto.*;
import com.instantchat.entity.User;
import com.instantchat.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId);
        return ApiResponse.success("登出成功", null);
    }

    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = authService.getCurrentUser(userId);
        return ApiResponse.success(user);
    }
}