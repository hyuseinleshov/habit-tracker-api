package com.habittracker.api.auth.service.impl;

import com.habittracker.api.auth.dto.*;
import com.habittracker.api.auth.exception.EmailAlreadyExistsException;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.security.jwt.service.JwtService;
import com.habittracker.api.userprofile.service.UserProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.habittracker.api.auth.utils.AuthConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final UserProfileService userProfileService;

  @Override
  public AuthResponse register(RegisterRequest request) {
    userRepository
        .findByEmail(request.email())
        .ifPresent(
            user -> {
              log.error("Email {} already exists", request.email());
              throw new EmailAlreadyExistsException(EMAIL_EXISTS_MESSAGE);
            });

    UserEntity user = new UserEntity();
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));

    RoleEntity role = roleRepository.getByType(RoleType.USER);
    user.setRoles(Collections.singleton(role));



    log.info("Registering new user: {}", request.email());
    UserEntity savedUser = userRepository.save(user);
    userProfileService.createProfile(user, request.timezone());

    refreshTokenService.revokeAllRefreshTokensForUser(savedUser.getEmail());
    String token = jwtService.generateToken(savedUser.getEmail());
    String refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());
    return new AuthResponse(savedUser.getEmail(), token, refreshToken, REGISTER_SUCCESS_MESSAGE);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    try {
      Authentication auth =
          authManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.email(), request.password()));

      if (auth.isAuthenticated()) {
        log.info("User authenticated: {}", request.email());
        refreshTokenService.revokeAllRefreshTokensForUser(request.email());
        String token = jwtService.generateToken(request.email());
        String refreshToken = refreshTokenService.createRefreshToken(request.email());
        return new AuthResponse(request.email(), token, refreshToken, LOGIN_SUCCESS_MESSAGE);
      }

      log.error("Authentication failed for user with email: {}", request.email());
      throw new BadCredentialsException(INVALID_CREDENTIALS_ERROR);
    } catch (AuthenticationException e) {
      log.error("Authentication error: {}", e.getMessage());
      throw new BadCredentialsException(INVALID_CREDENTIALS_ERROR);
    }
  }

  @Override
  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    String refreshToken = request.refreshToken();
    if (!refreshTokenService.isValid(refreshToken)) {
      throw new BadCredentialsException(INVALID_REFRESH_TOKEN_MESSAGE);
    }

    String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
    String newAccessToken = jwtService.generateToken(email);

    refreshTokenService.revokeRefreshToken(refreshToken);
    String newRefreshToken = refreshTokenService.createRefreshToken(email);
    return new RefreshTokenResponse(newAccessToken, newRefreshToken, REFRESH_SUCCESS_MESSAGE);
  }
}
