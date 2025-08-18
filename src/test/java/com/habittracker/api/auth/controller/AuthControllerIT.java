package com.habittracker.api.auth.controller;

import static com.habittracker.api.auth.utils.AuthConstants.*;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.auth.dto.RefreshTokenRequest;
import com.habittracker.api.auth.dto.RegisterRequest;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@BaseIntegrationTest
public class AuthControllerIT {

  @Autowired private AuthTestUtils authTestUtils;
  @Autowired private MockMvcTestUtils mockMvcTestUtils;
  private UserEntity testUser;

  @BeforeEach
  public void setUp() {
    testUser = authTestUtils.createAndSaveUser(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
    authTestUtils.createAndSaveUser(EXISTING_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
  }

  @Nested
  class RegisterTests {
    @Test
    public void givenValidDetails_whenRegister_thenSuccess() throws Exception {
      RegisterRequest request = new RegisterRequest(NEW_USER_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(NEW_USER_EMAIL)))
          .andExpect(jsonPath("$.token", notNullValue()))
          .andExpect(jsonPath("$.refreshToken", notNullValue()))
          .andExpect(jsonPath("$.message", is(REGISTER_SUCCESS_MESSAGE)));
    }

    @Test
    public void givenExistingEmail_whenRegister_thenError() throws Exception {
      RegisterRequest request = new RegisterRequest(EXISTING_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.email", is(EMAIL_EXISTS_MESSAGE)));
    }

    @Test
    public void givenDeletedEmail_whenRegister_thenError() throws Exception {
      RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
      authTestUtils.softDelete(testUser);
      String deleteDate =
          Instant.now()
              .atZone(ZoneId.of(TEST_TIMEZONE))
              .plusMonths(1)
              .format(DateTimeFormatter.ISO_LOCAL_DATE);
      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.errors.email", is(String.format(EMAIL_DELETED_MESSAGE, deleteDate))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@", "@domain.com", "user.domain.com"})
    public void givenInvalidEmail_whenRegister_thenValidationError(String invalidEmail)
        throws Exception {
      RegisterRequest request = new RegisterRequest(invalidEmail, TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    public void givenBlankEmail_whenRegister_thenValidationError() throws Exception {
      RegisterRequest request = new RegisterRequest("", TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(EMAIL_REQUIRED_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "short", "tiny"})
    public void givenShortPassword_whenRegister_thenValidationError(String shortPassword)
        throws Exception {
      RegisterRequest request = new RegisterRequest(NEW_USER_EMAIL, shortPassword, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", is(PASSWORD_LENGTH_MESSAGE)));
    }
  }

  @Nested
  class LoginTests {
    @Test
    public void givenValidCredentials_whenLogin_thenSuccess() throws Exception {
      RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.token", notNullValue()))
          .andExpect(jsonPath("$.refreshToken", notNullValue()))
          .andExpect(jsonPath("$.message", is(LOGIN_SUCCESS_MESSAGE)));
    }

    @Test
    public void givenInvalidPassword_whenLogin_thenUnauthorized() throws Exception {
      RegisterRequest request = new RegisterRequest(TEST_EMAIL, WRONG_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", is(INVALID_CREDENTIALS_MESSAGE)));
    }

    @Test
    public void givenNonexistentUser_whenLogin_thenUnauthorized() throws Exception {
      RegisterRequest request =
          new RegisterRequest(NONEXISTENT_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", is(INVALID_CREDENTIALS_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    public void givenBlankPassword_whenLogin_thenValidationError(String blankPassword)
        throws Exception {
      RegisterRequest request = new RegisterRequest(TEST_EMAIL, blankPassword, TEST_TIMEZONE);

      mockMvcTestUtils
          .performPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(
              jsonPath(
                  "$.errors.password",
                  anyOf(is(PASSWORD_REQUIRED_MESSAGE), is(PASSWORD_LENGTH_MESSAGE))));
    }
  }

  @Nested
  class RefreshTokenTests {
    @Test
    public void givenValidRefreshToken_whenRefresh_thenSuccess() throws Exception {
      RegisterRequest loginRequest = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
      String loginResponse =
          mockMvcTestUtils
              .performPostRequest(LOGIN_ENDPOINT, loginRequest)
              .andReturn()
              .getResponse()
              .getContentAsString();
      String refreshToken = new ObjectMapper().readTree(loginResponse).get("refreshToken").asText();

      RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
      mockMvcTestUtils
          .performPostRequest(REFRESH_ENDPOINT, refreshRequest)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken", notNullValue()))
          .andExpect(jsonPath("$.refreshToken", notNullValue()))
          .andExpect(jsonPath("$.message", is(REFRESH_SUCCESS_MESSAGE)));
    }

    @Test
    public void givenUsedRefreshToken_whenRefreshAgain_thenUnauthorized() throws Exception {
      RegisterRequest loginRequest = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
      String loginResponse =
          mockMvcTestUtils
              .performPostRequest(LOGIN_ENDPOINT, loginRequest)
              .andReturn()
              .getResponse()
              .getContentAsString();
      String refreshToken = new ObjectMapper().readTree(loginResponse).get("refreshToken").asText();

      RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
      String refreshResponse =
          mockMvcTestUtils
              .performPostRequest(REFRESH_ENDPOINT, refreshRequest)
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      String newRefreshToken =
          new ObjectMapper().readTree(refreshResponse).get("refreshToken").asText();

      mockMvcTestUtils
          .performPostRequest(REFRESH_ENDPOINT, refreshRequest)
          .andExpect(status().isUnauthorized());
    }
  }
}
