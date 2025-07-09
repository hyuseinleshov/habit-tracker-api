package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.utils.AuthConstants.*;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.exception.EmailAlreadyExistsException;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.security.jwt.service.JwtService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AuthResponse register(AuthRequest request) {
    userRepository
        .findByEmail(request.getEmail())
        .ifPresent(
            user -> {
              log.error("Email {} already exists", request.getEmail());
              throw new EmailAlreadyExistsException(EMAIL_EXISTS_MESSAGE);
            });

    UserEntity user = new UserEntity();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    RoleEntity role = roleRepository.findByType(RoleType.USER).orElseThrow();
    user.setRoles(Collections.singleton(role));

    log.info("Registering new user: {}", request.getEmail());
    UserEntity savedUser = userRepository.save(user);

    String token = jwtService.generateToken(savedUser.getEmail());

    return AuthResponse.builder()
        .token(token)
        .email(savedUser.getEmail())
        .message(REGISTER_SUCCESS_MESSAGE)
        .build();
  }

  @Override
  public AuthResponse login(AuthRequest request) {
    try {
      Authentication auth =
          authManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      if (auth.isAuthenticated()) {
        log.info("User authenticated: {}", request.getEmail());
        String token = jwtService.generateToken(request.getEmail());

        return AuthResponse.builder()
            .token(token)
            .email(request.getEmail())
            .message(LOGIN_SUCCESS_MESSAGE)
            .build();
      }

      log.error("Authentication failed for user with email: {}", request.getEmail());
      throw new BadCredentialsException(INVALID_CREDENTIALS_ERROR);
    } catch (AuthenticationException e) {
      log.error("Authentication error: {}", e.getMessage());
      throw new BadCredentialsException(INVALID_CREDENTIALS_ERROR);
    }
  }
}
