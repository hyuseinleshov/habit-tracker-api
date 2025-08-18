package com.habittracker.api.userprofile.controller;

import static com.habittracker.api.auth.testutils.MockMvcTestUtils.addJwt;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.auth.dto.RegisterRequest;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.testutils.RoleTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.security.jwt.testutils.JwtTestUtils;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@BaseIntegrationTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserProfileControllerIT {

  @Autowired private SecretKey secretKey;

  @Value("${jwt.issuer}")
  private String issuer;

  @Autowired private MockMvc mockMvc;

  @Autowired private AuthService authService;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private RoleTestUtils roleTestUtils;

  @BeforeAll
  void setupRoles() {
    roleTestUtils.setUpRoles();
  }

  @Test
  public void test_Get_UserProfile_Return_Unauthorized_When_NotHave_Jwt() throws Exception {
    mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
  }

  @Test
  public void test_getUserProfile_Return_ExpectedResult_With_JWT() throws Exception {
    authService.register(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE));
    String jwt = JwtTestUtils.generateValidToken(TEST_EMAIL, issuer, secretKey);
    mockMvc
        .perform(addJwt(jwt, get("/api/me")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.timezone").value(TEST_TIMEZONE));
  }

  @Test
  public void test_Update_UserProfile_Return_Unauthorized_When_NotHave_Jwt() throws Exception {
    mockMvc.perform(put("/api/me")).andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @MethodSource(
      "com.habittracker.api.userprofile.testutils.UserProfileTestUtils#invalidUserProfileDTOs")
  public void test_Update_UserProfile_Return_Bad_InvalidBody(UserProfileDTO profileDTO)
      throws Exception {
    authService.register(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE));
    String jwt = JwtTestUtils.generateValidToken(TEST_EMAIL, issuer, secretKey);
    mockMvc
        .perform(
            addJwt(
                jwt,
                put("/api/me")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(profileDTO))))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void test_Update_UserProfile_Return_ExpectedResult_WithValidUserProfile()
      throws Exception {
    final String UPDATED_TIMEZONE = "Asia/Tokyo";
    final String UPDATED_FIRST_NAME = "John";
    final String UPDATED_LAST_NAME = "Doe";
    final Integer UPDATED_AGE = 22;
    UserProfileDTO userProfileDTO =
        new UserProfileDTO(UPDATED_FIRST_NAME, UPDATED_LAST_NAME, UPDATED_AGE, UPDATED_TIMEZONE);
    authService.register(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE));
    String jwt = JwtTestUtils.generateValidToken(TEST_EMAIL, issuer, secretKey);
    mockMvc
        .perform(
            addJwt(
                jwt,
                put("/api/me")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(userProfileDTO))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value(UPDATED_FIRST_NAME))
        .andExpect(jsonPath("$.lastName").value(UPDATED_LAST_NAME))
        .andExpect(jsonPath("$.age").value(UPDATED_AGE))
        .andExpect(jsonPath("$.timezone").value(UPDATED_TIMEZONE));
  }

  @Test
  public void test_Delete_UserProfile_Return_Unauthorized_When_NotHave_Jwt() throws Exception {
    mockMvc.perform(delete("/api/me")).andExpect(status().isUnauthorized());
  }

  @Test
  public void test_Delete_UserProfile_DeleteUser_WithValid_JWT() throws Exception {
    authService.register(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE));
    String jwt = JwtTestUtils.generateValidToken(TEST_EMAIL, issuer, secretKey);
    mockMvc
            .perform(addJwt(jwt, delete("/api/me")))
            .andExpect(status().isNoContent());
  }
}
