package com.habittracker.api.auth.controller;

import static com.habittracker.api.auth.utils.AuthConstants.*;
import static com.habittracker.api.auth.utils.AuthTestUtils.*;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.dto.AuthResponse;
import com.habittracker.api.auth.exception.EmailAlreadyExistsException;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.config.annotation.WebMvcTestWithoutJwt;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTestWithoutJwt(AuthController.class)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthService authService;

  private AuthRequest validRequest;
  private AuthResponse successRegisterResponse;
  private AuthResponse successLoginResponse;

  @BeforeEach
  void setUp() {
    validRequest = new AuthRequest(TEST_EMAIL, TEST_PASSWORD);
    successRegisterResponse = new AuthResponse(TEST_EMAIL, JWT_TOKEN, REGISTER_SUCCESS_MESSAGE);
    successLoginResponse = new AuthResponse(TEST_EMAIL, JWT_TOKEN, LOGIN_SUCCESS_MESSAGE);
  }

  private ResultActions doPostRequest(String endpoint, AuthRequest request) throws Exception {
    return performPostRequest(mockMvc, endpoint, request);
  }

  private ResultActions doPostRequestWithRawJson(String endpoint, String jsonContent)
      throws Exception {
    return mockMvc.perform(
        post(endpoint).contentType(MediaType.APPLICATION_JSON).content(jsonContent));
  }

  @Nested
  class RegisterTests {
    @Test
    void givenValidRequest_whenRegister_thenReturnCreatedWithToken() throws Exception {
      when(authService.register(any(AuthRequest.class))).thenReturn(successRegisterResponse);

      doPostRequest(REGISTER_ENDPOINT, validRequest)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.token", is(JWT_TOKEN)))
          .andExpect(jsonPath("$.message", is(REGISTER_SUCCESS_MESSAGE)));

      verify(authService).register(any(AuthRequest.class));
    }

    @Test
    void givenExistingEmail_whenRegister_thenReturnConflict() throws Exception {
      when(authService.register(any(AuthRequest.class)))
          .thenThrow(new EmailAlreadyExistsException(EMAIL_EXISTS_MESSAGE));

      doPostRequest(REGISTER_ENDPOINT, validRequest)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message", is(EMAIL_EXISTS_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@", "@domain.com", "user.domain.com"})
    void givenInvalidEmail_whenRegister_thenReturnBadRequest(String invalidEmail) throws Exception {
      AuthRequest invalidRequest = new AuthRequest(invalidEmail, TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    void givenBlankEmail_whenRegister_thenReturnBadRequest() throws Exception {
      AuthRequest invalidRequest = new AuthRequest("", TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(EMAIL_REQUIRED_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "short", "tiny"})
    void givenShortPassword_whenRegister_thenReturnBadRequest(String shortPassword)
        throws Exception {
      AuthRequest invalidRequest = new AuthRequest(TEST_EMAIL, shortPassword);

      doPostRequest(REGISTER_ENDPOINT, invalidRequest)
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

      doPostRequestWithRawJson(REGISTER_ENDPOINT, jsonContent)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    void givenMalformedJson_whenRegister_thenReturnBadRequest() throws Exception {
      doPostRequestWithRawJson(REGISTER_ENDPOINT, MALFORMED_JSON)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(MALFORMED_JSON_MESSAGE)));
    }
  }

  @Nested
  class LoginTests {
    @Test
    void givenValidCredentials_whenLogin_thenReturnOkWithToken() throws Exception {
      when(authService.login(any(AuthRequest.class))).thenReturn(successLoginResponse);

      doPostRequest(LOGIN_ENDPOINT, validRequest)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.token", is(JWT_TOKEN)))
          .andExpect(jsonPath("$.message", is(LOGIN_SUCCESS_MESSAGE)));

      verify(authService).login(any(AuthRequest.class));
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturnUnauthorized() throws Exception {
      when(authService.login(any(AuthRequest.class)))
          .thenThrow(new BadCredentialsException(INVALID_CREDENTIALS_ERROR));

      doPostRequest(LOGIN_ENDPOINT, validRequest)
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

      doPostRequestWithRawJson(LOGIN_ENDPOINT, jsonContent)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "missing-at-sign.com", "user@", "@domain.com"})
    void givenInvalidEmail_whenLogin_thenReturnBadRequest(String invalidEmail) throws Exception {
      AuthRequest invalidRequest = new AuthRequest(invalidEmail, TEST_PASSWORD);

      doPostRequest(LOGIN_ENDPOINT, invalidRequest)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    void givenMissingRequestBody_whenLogin_thenReturnBadRequest() throws Exception {
      doPostRequestWithRawJson(LOGIN_ENDPOINT, EMPTY_JSON)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", notNullValue()))
          .andExpect(jsonPath("$.errors.password", notNullValue()));
    }
  }
}
