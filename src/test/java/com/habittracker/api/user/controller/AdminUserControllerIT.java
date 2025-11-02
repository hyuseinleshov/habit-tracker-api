package com.habittracker.api.user.controller;

import static com.habittracker.api.auth.testutils.MockMvcTestUtils.addJwt;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.auth.testutils.RoleTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.security.jwt.service.JwtService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@BaseIntegrationTest
@Transactional
class AdminUserControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private AuthTestUtils authTestUtils;

  @Autowired private RoleTestUtils roleTestUtils;

  @Autowired private JwtService jwtService;

  private UserEntity adminUser;
  private UserEntity regularUser;
  private String adminJwt;
  private String regularUserJwt;

  @BeforeEach
  void setUp() {
    roleTestUtils.setUpRoles();

    regularUser = authTestUtils.createAndSaveUser(TEST_EMAIL, TEST_PASSWORD, TEST_TIMEZONE);
    regularUserJwt = jwtService.generateToken(regularUser);

    adminUser =
        authTestUtils.createAndSaveUser(
            TEST_ADMIN_EMAIL, TEST_PASSWORD, TEST_TIMEZONE, RoleType.ADMIN);
    adminJwt = jwtService.generateToken(adminUser);
  }

  @Test
  void getAllUsers_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
    mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
  }

  @Test
  void getAllUsers_WithRegularUser_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(addJwt(regularUserJwt, get("/api/users"))).andExpect(status().isForbidden());
  }

  @Test
  void getAllUsers_WithAdminUser_ShouldReturnAllUsers() throws Exception {
    mockMvc
        .perform(addJwt(adminJwt, get("/api/users")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].email").isNotEmpty())
        .andExpect(jsonPath("$.content[0].timezone").isNotEmpty());
  }

  @Test
  void getAllUsers_WithAdminUser_ShouldIncludeDeletedUsers() throws Exception {
    authTestUtils.softDelete(regularUser, Instant.now());

    mockMvc
        .perform(addJwt(adminJwt, get("/api/users")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[?(@.email == '" + TEST_EMAIL + "')].deletedAt").exists());
  }

  @Test
  void getAllUsers_WithPagination_ShouldReturnPagedResults() throws Exception {
    authTestUtils.createAndSaveUser("user3@example.com", TEST_PASSWORD, TEST_TIMEZONE);
    authTestUtils.createAndSaveUser("user4@example.com", TEST_PASSWORD, TEST_TIMEZONE);

    mockMvc
        .perform(addJwt(adminJwt, get("/api/users").param("page", "0").param("size", "2")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(4));
  }

  @Test
  void getAllUsers_WithSorting_ShouldReturnSortedResults() throws Exception {
    mockMvc
        .perform(
            addJwt(adminJwt, get("/api/users").param("sort", "createdAt,desc").param("size", "10")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)));
  }
}
