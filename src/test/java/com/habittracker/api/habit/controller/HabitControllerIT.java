package com.habittracker.api.habit.controller;

import static com.habittracker.api.habit.constants.HabitConstants.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.testutils.HabitTestUtils;
import com.habittracker.api.security.jwt.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@BaseIntegrationTest
public class HabitControllerIT {

  @Autowired
  private MockMvcTestUtils mockMvcTestUtils;
  @Autowired
  private AuthTestUtils authTestUtils;
  @Autowired
  private HabitTestUtils habitTestUtils;
  @Autowired
  private JwtService jwtService;

  private UserEntity testUser;
  private String authToken;

  @BeforeEach
  public void setUp() {
    testUser = authTestUtils.createAndSaveUser("test@example.com", "password123", "UTC");
    authToken = "Bearer " + jwtService.generateToken(testUser.getEmail());
  }

  @Nested
  class CreateHabitTests {

    @Test
    public void shouldCreateHabit_WhenValidRequest() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest("Read daily", "Read for 30 minutes");

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Read daily"))
          .andExpect(jsonPath("$.description").value("Read for 30 minutes"))
          .andExpect(jsonPath("$.frequency").value("DAILY"))
          .andExpect(jsonPath("$.archived").value(false))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldCreateHabitWithNullDescription_WhenDescriptionNotProvided() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest("Exercise", null);

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Exercise"))
          .andExpect(jsonPath("$.description").isEmpty())
          .andExpect(jsonPath("$.frequency").value("DAILY"))
          .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    public void shouldReturnConflict_WhenHabitNameAlreadyExists() throws Exception {
      habitTestUtils.createAndSaveHabit(testUser, "Read daily");
      CreateHabitRequest request = new CreateHabitRequest("Read daily", "Another description");

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(NAME_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    public void shouldReturnConflictIgnoreCase_WhenHabitNameExistsWithDifferentCase()
        throws Exception {
      habitTestUtils.createAndSaveHabit(testUser, "read daily");
      CreateHabitRequest request = new CreateHabitRequest("READ DAILY", "Description");

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(NAME_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenNameIsBlank() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest("", "Description");

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value("Validation failed for one or more fields in your request."))
          .andExpect(jsonPath("$.errors.name").value(NAME_REQUIRED_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenNameTooLong() throws Exception {
      String longName = "a".repeat(101);
      CreateHabitRequest request = new CreateHabitRequest(longName, "Description");

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.name").value(NAME_LENGTH_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenDescriptionTooLong() throws Exception {
      String longDescription = "a".repeat(2001);
      CreateHabitRequest request = new CreateHabitRequest("Valid name", longDescription);

      mockMvcTestUtils
          .performAuthenticatedPostRequest("/api/habits", request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.description").value(DESCRIPTION_LENGTH_MESSAGE));
    }

    @Test
    public void shouldReturnUnauthorized_WhenNoAuthToken() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest("Read daily", "Description");

      mockMvcTestUtils
          .performUnauthenticatedPostRequest("/api/habits", request)
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class GetUserHabitsTests {

    @Test
    public void shouldReturnUserHabits_WhenHabitsExist() throws Exception {
      habitTestUtils.createAndSaveHabit(testUser, "Read daily", "Read for 30 minutes");
      habitTestUtils.createAndSaveHabit(testUser, "Exercise", "Workout for 45 minutes");

      mockMvcTestUtils
          .performAuthenticatedGetRequest("/api/habits", authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[*].name", containsInAnyOrder("Read daily", "Exercise")))
          .andExpect(jsonPath("$[*].frequency", everyItem(is("DAILY"))))
          .andExpect(jsonPath("$[*].archived", everyItem(is(false))));
    }

    @Test
    public void shouldReturnEmptyList_WhenNoHabitsExist() throws Exception {
      mockMvcTestUtils
          .performAuthenticatedGetRequest("/api/habits", authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldOnlyReturnCurrentUserHabits_WhenMultipleUsersHaveHabits() throws Exception {
      UserEntity otherUser = authTestUtils.createAndSaveUser("other@example.com", "password123", "UTC");
      habitTestUtils.createAndSaveHabit(testUser, "My habit");
      habitTestUtils.createAndSaveHabit(otherUser, "Other's habit");

      mockMvcTestUtils
          .performAuthenticatedGetRequest("/api/habits", authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].name").value("My habit"));
    }

    @Test
    public void shouldReturnUnauthorized_WhenNoAuthToken() throws Exception {
      mockMvcTestUtils
          .performUnauthenticatedGetRequest("/api/habits")
          .andExpect(status().isUnauthorized());
    }
  }
}
