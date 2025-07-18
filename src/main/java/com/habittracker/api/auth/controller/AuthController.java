package com.habittracker.api.auth.controller;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
    AuthResponse response = userService.register(request);
    log.info("User with email - {}, registered successfully", request.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    AuthResponse response = userService.login(request);
    log.info("User with email - {}, logged in successfully", request.email());
    return ResponseEntity.ok(response);
  }
}
