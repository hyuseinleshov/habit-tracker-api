package com.habittracker.api.auth.controller;

import com.habittracker.api.auth.dto.*;
import com.habittracker.api.auth.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService userService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = userService.register(request);
    log.info("User with email - {}, registered successfully", request.email());
    return ResponseEntity.created(URI.create("/api/me")).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = userService.login(request);
    log.info("User with email - {}, logged in successfully", request.email());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    RefreshTokenResponse response = userService.refreshToken(request);
    log.info("Refresh token used successfully");
    return ResponseEntity.ok(response);
  }
}
