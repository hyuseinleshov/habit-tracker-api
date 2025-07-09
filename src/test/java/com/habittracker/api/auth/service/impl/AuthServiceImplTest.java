package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.utils.AuthConstants.*;
import static com.habittracker.api.auth.utils.AuthTestConstants.*;
import static com.habittracker.api.auth.utils.AuthTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.exception.EmailAlreadyExistsException;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.security.jwt.service.JwtService;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private AuthenticationManager authManager;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private AuthServiceImpl authService;

  private AuthRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest = createAuthRequest(TEST_EMAIL, TEST_PASSWORD);
  }

  @Nested
  class Register {
    @Test
    void givenValidCredentials_whenRegisteringNewUser_thenSuccessful() {
      setupForSuccessfulRegistration();

      AuthResponse response = authService.register(validRequest);

      assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
      assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
      assertThat(response.getMessage()).isEqualTo(REGISTER_SUCCESS_MESSAGE);
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void givenExistingEmail_whenRegisteringUser_thenThrowsException() {
      when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(new UserEntity()));

      assertThatThrownBy(() -> authService.register(validRequest))
          .isInstanceOf(EmailAlreadyExistsException.class)
          .hasMessage(EMAIL_EXISTS_MESSAGE);

      verify(userRepository, never()).save(any());
    }

    @Test
    void givenValidCredentials_whenRoleNotFound_thenThrowsException() {
      when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
      when(roleRepository.findByType(RoleType.USER)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.register(validRequest))
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void givenValidCredentials_whenRegisteringUser_thenSavesUserWithCorrectProperties() {
      when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
      when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
      when(roleRepository.findByType(RoleType.USER)).thenReturn(Optional.of(createUserRole()));
      when(userRepository.save(any())).thenReturn(new UserEntity());

      ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

      authService.register(validRequest);

      verify(userRepository).save(userCaptor.capture());
      UserEntity capturedUser = userCaptor.getValue();
      assertThat(capturedUser.getEmail()).isEqualTo(TEST_EMAIL);
      assertThat(capturedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
      assertThat(capturedUser.getPassword()).isNotEqualTo(TEST_PASSWORD);
    }
  }

  @Nested
  class Login {
    @Test
    void givenValidCredentials_whenLoggingIn_thenSuccessful() {
      setupForSuccessfulAuthentication();

      AuthResponse response = authService.login(validRequest);

      assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
      assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
      assertThat(response.getMessage()).isEqualTo(LOGIN_SUCCESS_MESSAGE);
    }

    @Test
    void givenInvalidCredentials_whenLoggingIn_thenThrowsException() {
      when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(new BadCredentialsException(INVALID_CREDENTIALS_ERROR));

      assertThatThrownBy(() -> authService.login(validRequest))
          .isInstanceOf(BadCredentialsException.class)
          .hasMessage(INVALID_CREDENTIALS_ERROR);
    }

    @Test
    void givenValidCredentialsButNotAuthenticated_whenLoggingIn_thenThrowsException() {
      Authentication auth = mock(Authentication.class);
      when(authManager.authenticate(any())).thenReturn(auth);
      when(auth.isAuthenticated()).thenReturn(false);

      assertThatThrownBy(() -> authService.login(validRequest))
          .isInstanceOf(BadCredentialsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid"})
    void givenInvalidEmail_whenLoggingIn_thenThrowsException(String invalidEmail) {
      AuthRequest invalidRequest = createAuthRequest(invalidEmail, TEST_PASSWORD);

      when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);

      assertThatThrownBy(() -> authService.login(invalidRequest))
          .isInstanceOf(BadCredentialsException.class);
    }
  }

  private void setupForSuccessfulRegistration() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

    RoleEntity role = createUserRole();
    when(roleRepository.findByType(RoleType.USER)).thenReturn(Optional.of(role));

    UserEntity savedUser = createUser(TEST_EMAIL, ENCODED_PASSWORD, role);
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
    when(jwtService.generateToken(TEST_EMAIL)).thenReturn(JWT_TOKEN);
  }

  private void setupForSuccessfulAuthentication() {
    Authentication auth = mock(Authentication.class);
    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
    when(auth.isAuthenticated()).thenReturn(true);
    when(jwtService.generateToken(TEST_EMAIL)).thenReturn(JWT_TOKEN);
  }
}
