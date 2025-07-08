package com.habittracker.api.auth.controller;

import static com.habittracker.api.auth.utils.AuthTestConstants.*;
import static com.habittracker.api.auth.utils.AuthTestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.common.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AuthControllerIT extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private RoleEntity userRole;

  @BeforeEach
  public void setUp() {
    setupUserRole();
    createTestUsers();
  }

  private void setupUserRole() {
    userRole = getUserRoleFromRepository(roleRepository);
  }

  private void createTestUsers() {
    createUser(EXISTING_EMAIL, TEST_PASSWORD);
    createUser(TEST_EMAIL, TEST_PASSWORD);
  }

  private UserEntity createUser(String email, String password) {
    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.getRoles().add(userRole);
    return userRepository.save(user);
  }

  private ResultActions doPostRequest(String endpoint, AuthRequest request) throws Exception {
    return performPostRequest(mockMvc, endpoint, request);
  }

  @Nested
  class RegisterTests {
    @Test
    public void givenValidDetails_whenRegister_thenSuccess() throws Exception {
      AuthRequest request = createAuthRequest(NEW_USER_EMAIL, TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(NEW_USER_EMAIL)))
          .andExpect(jsonPath("$.token", notNullValue()))
          .andExpect(jsonPath("$.message", is(REGISTER_SUCCESS_MESSAGE)));
    }

    @Test
    public void givenExistingEmail_whenRegister_thenError() throws Exception {
      AuthRequest request = createAuthRequest(EXISTING_EMAIL, TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message", is(EMAIL_EXISTS_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@", "@domain.com", "user.domain.com"})
    public void givenInvalidEmail_whenRegister_thenValidationError(String invalidEmail)
        throws Exception {
      AuthRequest request = createAuthRequest(invalidEmail, TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(INVALID_EMAIL_MESSAGE)));
    }

    @Test
    public void givenBlankEmail_whenRegister_thenValidationError() throws Exception {
      AuthRequest request = createAuthRequest("", TEST_PASSWORD);

      doPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.email", is(EMAIL_REQUIRED_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "short", "tiny"})
    public void givenShortPassword_whenRegister_thenValidationError(String shortPassword)
        throws Exception {
      AuthRequest request = createAuthRequest(NEW_USER_EMAIL, shortPassword);

      doPostRequest(REGISTER_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(jsonPath("$.errors.password", is(PASSWORD_LENGTH_MESSAGE)));
    }
  }

  @Nested
  class LoginTests {
    @Test
    public void givenValidCredentials_whenLogin_thenSuccess() throws Exception {
      AuthRequest request = createAuthRequest(TEST_EMAIL, TEST_PASSWORD);

      doPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.token", notNullValue()))
          .andExpect(jsonPath("$.message", is(LOGIN_SUCCESS_MESSAGE)));
    }

    @Test
    public void givenInvalidPassword_whenLogin_thenUnauthorized() throws Exception {
      AuthRequest request = createAuthRequest(TEST_EMAIL, WRONG_PASSWORD);

      doPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", is(INVALID_CREDENTIALS_MESSAGE)));
    }

    @Test
    public void givenNonexistentUser_whenLogin_thenUnauthorized() throws Exception {
      AuthRequest request = createAuthRequest(NONEXISTENT_EMAIL, TEST_PASSWORD);

      doPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", is(INVALID_CREDENTIALS_MESSAGE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    public void givenBlankPassword_whenLogin_thenValidationError(String blankPassword)
        throws Exception {
      AuthRequest request = createAuthRequest(TEST_EMAIL, blankPassword);

      doPostRequest(LOGIN_ENDPOINT, request)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message", is(VALIDATION_FAILED_MESSAGE)))
          .andExpect(
              jsonPath(
                  "$.errors.password",
                  anyOf(is(PASSWORD_REQUIRED_MESSAGE), is(PASSWORD_LENGTH_MESSAGE))));
    }
  }
}
