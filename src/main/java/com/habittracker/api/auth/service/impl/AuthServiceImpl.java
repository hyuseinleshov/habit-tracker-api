package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.utils.AuthConstants.*;

import com.habittracker.api.auth.dto.*;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.security.jwt.service.JwtService;
import com.habittracker.api.user.model.UserProfileEntity;
import com.habittracker.api.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final UserProfileService userProfileService;

  @Override
  public AuthResponse register(@Valid RegisterRequest request) {

    UserEntity user = new UserEntity();
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));

    RoleEntity role = roleRepository.getByType(RoleType.USER);
    user.getRoles().add(role);

    log.info("Registering new user: {}", request.email());
    UserProfileEntity profile = userProfileService.createProfile(user, request.timeZone());
    user.setUserProfile(profile);
    UserEntity savedUser = userRepository.save(user);

    refreshTokenService.revokeAllRefreshTokensForUser(savedUser.getId());
    String token = jwtService.generateToken(savedUser);
    String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
    return new AuthResponse(savedUser.getEmail(), token, refreshToken, REGISTER_SUCCESS_MESSAGE);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    UserEntity user =
        userRepository
            .findByEmailAndDeletedAtIsNull(request.email())
            .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS_ERROR));

    log.info("User authenticated: {}", request.email());
    refreshTokenService.revokeAllRefreshTokensForUser(user.getId());
    String token = jwtService.generateToken(user);
    String refreshToken = refreshTokenService.createRefreshToken(user.getId());
    return new AuthResponse(request.email(), token, refreshToken, LOGIN_SUCCESS_MESSAGE);
  }

  @Override
  public RefreshTokenResponse refreshToken(String refreshToken) {
    if (!refreshTokenService.isValid(refreshToken)) {
      throw new BadCredentialsException(INVALID_REFRESH_TOKEN_MESSAGE);
    }

    UserEntity userEntity =
        refreshTokenService
            .getUserIdFromRefreshToken(refreshToken)
            .map(userRepository::getReferenceById)
            .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN_MESSAGE));

    String newAccessToken = jwtService.generateToken(userEntity);

    refreshTokenService.revokeRefreshToken(refreshToken);
    String newRefreshToken = refreshTokenService.createRefreshToken(userEntity.getId());
    return new RefreshTokenResponse(newAccessToken, newRefreshToken, REFRESH_SUCCESS_MESSAGE);
  }
}
