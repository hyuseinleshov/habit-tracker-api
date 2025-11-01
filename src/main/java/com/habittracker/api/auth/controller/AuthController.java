package com.habittracker.api.auth.controller;

import com.habittracker.api.auth.dto.*;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.utils.RefreshTokenCookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService userService;
  private final RefreshTokenCookieUtils refreshTokenCookieUtils;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
    AuthResponse response = userService.register(request);
    log.info("User with email - {}, registered successfully", request.email());
    refreshTokenCookieUtils.addRefreshTokenCookie(response.refreshToken(), httpResponse);
    return ResponseEntity.created(URI.create("/api/me")).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
    AuthResponse response = userService.login(request);
    log.info("User with email - {}, logged in successfully", request.email());
    refreshTokenCookieUtils.addRefreshTokenCookie(response.refreshToken(), httpResponse);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refresh(
      @CookieValue("refreshToken") String refreshToken, HttpServletResponse httpResponse) {
    RefreshTokenResponse response = userService.refreshToken(refreshToken);
    log.info("Refresh token used successfully");
    refreshTokenCookieUtils.addRefreshTokenCookie(response.refreshToken(), httpResponse);
    return ResponseEntity.ok(response);
  }
}
