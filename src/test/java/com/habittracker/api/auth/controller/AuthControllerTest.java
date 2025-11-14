package com.habittracker.api.auth.controller;

import static com.habittracker.api.auth.utils.AuthConstants.*;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.auth.dto.*;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.annotation.WebMvcTestWithoutSecurity;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTestWithoutSecurity(AuthController.class)
public class AuthControllerTest {

  @Autowired private MockMvcTestUtils mockMvcTestUtils;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthService authService;
  @MockitoBean private UserRepository userRepository;

  private RegisterRequest validRequest;
  private AuthResponse successRegisterResponse;
  private AuthResponse successLoginResponse;

  @BeforeEach
  void setUp() {
    validRequest = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIME_ZONE);
    successRegisterResponse =
        new AuthResponse(TEST_EMAIL, JWT_TOKEN, REFRESH_TOKEN, REGISTER_SUCCESS_MESSAGE);
    successLoginResponse =
        new AuthResponse(TEST_EMAIL, JWT_TOKEN, REFRESH_TOKEN, LOGIN_SUCCESS_MESSAGE);
  }

  @Nested
  class RegisterTests {
    @Test
    void givenValidRequest_whenRegister_thenReturnCreatedWithToken() throws Exception {
      when(authService.register(any(RegisterRequest.class))).thenReturn(successRegisterResponse);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, validRequest)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.accessToken", is(JWT_TOKEN)))
          .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
          .andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE_NAME, true))
          .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
          .andExpect(jsonPath("$.message", is(REGISTER_SUCCESS_MESSAGE)));

      verify(authService).register(any(RegisterRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@", "@domain.com", "user.domain.com"})
    void givenInvalidEmail_whenRegister_thenReturnBadRequest(String invalidEmail) throws Exception {
      RegisterRequest invalidRequest =
          new RegisterRequest(invalidEmail, TEST_PASSWORD, TEST_TIME_ZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    void givenBlankEmail_whenRegister_thenReturnBadRequest() throws Exception {
      RegisterRequest invalidRequest = new RegisterRequest("", TEST_PASSWORD, TEST_TIME_ZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(EMAIL_REQUIRED_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "short", "tiny"})
    void givenShortPassword_whenRegister_thenReturnBadRequest(String shortPassword)
        throws Exception {
      RegisterRequest invalidRequest =
          new RegisterRequest(TEST_EMAIL, shortPassword, TEST_TIME_ZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", is(PASSWORD_LENGTH_MESSAGE)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenNullOrEmptyPassword_whenRegister_thenReturnBadRequest(String invalidPassword)
        throws Exception {
      Map<String, String> requestMap = new HashMap<>();
      requestMap.put("email", TEST_EMAIL);
      if (invalidPassword != null) {
        requestMap.put("password", invalidPassword);
      }
      String jsonContent = objectMapper.writeValueAsString(requestMap);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, jsonContent)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    void givenMalformedJson_whenRegister_thenReturnBadRequest() throws Exception {
      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, MALFORMED_JSON)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(MALFORMED_JSON_MESSAGE)));
    }
  }

  @Nested
  class LoginTests {
    @Test
    void givenValidCredentials_whenLogin_thenReturnOkWithToken() throws Exception {
      when(authService.login(any(LoginRequest.class))).thenReturn(successLoginResponse);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, validRequest)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.accessToken", is(JWT_TOKEN)))
          .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
          .andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE_NAME, true))
          .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, successLoginResponse.refreshToken()))
          .andExpect(jsonPath("$.message", is(LOGIN_SUCCESS_MESSAGE)));

      verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturnUnauthorized() throws Exception {
      when(authService.login(any(LoginRequest.class)))
          .thenThrow(new BadCredentialsException(INVALID_CREDENTIALS_ERROR));

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, validRequest)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", is(INVALID_CREDENTIALS_MESSAGE)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenBlankOrNullPassword_whenLogin_thenReturnBadRequest(String blankPassword)
        throws Exception {
      Map<String, String> requestMap = new HashMap<>();
      requestMap.put("email", TEST_EMAIL);
      if (blankPassword != null) {
        requestMap.put("password", blankPassword);
      }
      String jsonContent = objectMapper.writeValueAsString(requestMap);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, jsonContent)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "missing-at-sign.com", "user@", "@domain.com"})
    void givenInvalidEmail_whenLogin_thenReturnBadRequest(String invalidEmail) throws Exception {
      RegisterRequest invalidRequest =
          new RegisterRequest(invalidEmail, TEST_PASSWORD, TEST_TIME_ZONE);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    void givenMissingRequestBody_whenLogin_thenReturnBadRequest() throws Exception {
      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, EMPTY_JSON)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", notNullValue()))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }
  }

  @Nested
  class RefreshTokenTests {
    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnNewTokens() throws Exception {
      RefreshTokenResponse refreshResponse =
          new RefreshTokenResponse(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN, REFRESH_SUCCESS_MESSAGE);
      when(authService.refreshToken(any(String.class))).thenReturn(refreshResponse);

      Cookie refreshTokenCookie =
          new Cookie(REFRESH_TOKEN_COOKIE_NAME, UUID.randomUUID().toString());
      refreshTokenCookie.setHttpOnly(true);

      mockMvcTestUtils
          .performPostRequest(REFRESH_ENDPOINT, EMPTY_JSON, refreshTokenCookie)
          .andExpect(status().isOk())
          .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
          .andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE_NAME, true))
          .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, NEW_REFRESH_TOKEN))
          .andExpect(jsonPath("$.accessToken", is(NEW_ACCESS_TOKEN)))
          .andExpect(jsonPath("$.message", is(REFRESH_SUCCESS_MESSAGE)));

      verify(authService).refreshToken(any(String.class));
    }
  }
}
