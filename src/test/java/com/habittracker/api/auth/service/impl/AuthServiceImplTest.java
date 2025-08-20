package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.auth.utils.AuthConstants.*;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.dto.LoginRequest;
import com.habittracker.api.auth.dto.RegisterRequest;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.security.jwt.service.JwtService;
import com.habittracker.api.user.service.UserProfileService;
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
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private UserProfileService userProfileService;
  @InjectMocks private AuthServiceImpl authService;

  private RegisterRequest validRegisterRequest;
  private LoginRequest validLoginRequest;

  @BeforeEach
  void setUp() {
    validRegisterRequest = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
    validLoginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
  }

  @Nested
  class Register {
    @Test
    void givenValidCredentials_whenRegisteringNewUser_thenSuccessful() {
      setupForSuccessfulRegistration();

      AuthResponse response = authService.register(validRegisterRequest);

      assertThat(response.email()).isEqualTo(TEST_EMAIL);
      assertThat(response.token()).isEqualTo(JWT_TOKEN);
      assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
      assertThat(response.message()).isEqualTo(REGISTER_SUCCESS_MESSAGE);
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void givenValidCredentials_whenRegisteringUser_thenSavesUserWithCorrectProperties() {
      when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
      when(roleRepository.getByType(RoleType.USER)).thenReturn(createUserRole());
      when(userRepository.save(any())).thenReturn(new UserEntity());

      ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

      authService.register(validRegisterRequest);

      verify(userRepository).save(userCaptor.capture());
      UserEntity capturedUser = userCaptor.getValue();
      verify(userProfileService).createProfile(capturedUser, TEST_TIMEZONE);
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

      AuthResponse response = authService.login(validLoginRequest);

      assertThat(response.email()).isEqualTo(TEST_EMAIL);
      assertThat(response.token()).isEqualTo(JWT_TOKEN);
      assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
      assertThat(response.message()).isEqualTo(LOGIN_SUCCESS_MESSAGE);
    }

    @Test
    void givenInvalidCredentials_whenLoggingIn_thenThrowsException() {
      when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(new BadCredentialsException(INVALID_CREDENTIALS_ERROR));

      assertThatThrownBy(() -> authService.login(validLoginRequest))
          .isInstanceOf(BadCredentialsException.class)
          .hasMessage(INVALID_CREDENTIALS_ERROR);
    }

    @Test
    void givenValidCredentialsButNotAuthenticated_whenLoggingIn_thenThrowsException() {
      Authentication auth = mock(Authentication.class);
      when(authManager.authenticate(any())).thenReturn(auth);
      when(auth.isAuthenticated()).thenReturn(false);

      assertThatThrownBy(() -> authService.login(validLoginRequest))
          .isInstanceOf(BadCredentialsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid"})
    void givenInvalidEmail_whenLoggingIn_thenThrowsException(String invalidEmail) {
      LoginRequest invalidRequest = new LoginRequest(invalidEmail, TEST_PASSWORD);

      when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);

      assertThatThrownBy(() -> authService.login(invalidRequest))
          .isInstanceOf(BadCredentialsException.class);
    }
  }

  private void setupForSuccessfulRegistration() {
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

    RoleEntity role = createUserRole();
    when(roleRepository.getByType(RoleType.USER)).thenReturn(role);

    UserEntity savedUser = createUser(TEST_EMAIL, ENCODED_PASSWORD, role);
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
    when(jwtService.generateToken(TEST_EMAIL)).thenReturn(JWT_TOKEN);
    when(refreshTokenService.createRefreshToken(TEST_EMAIL)).thenReturn(REFRESH_TOKEN);
  }

  private void setupForSuccessfulAuthentication() {
    Authentication auth = mock(Authentication.class);
    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
    when(auth.isAuthenticated()).thenReturn(true);
    when(jwtService.generateToken(TEST_EMAIL)).thenReturn(JWT_TOKEN);
    when(refreshTokenService.createRefreshToken(TEST_EMAIL)).thenReturn(REFRESH_TOKEN);
  }
}
