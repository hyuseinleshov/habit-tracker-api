package com.habittracker.api.habit.controller;

import static com.habittracker.api.auth.testutils.MockMvcTestUtils.addJwt;
import static com.habittracker.api.habit.constants.HabitConstants.*;
import static com.habittracker.api.habit.constants.HabitTestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.testutils.AuthTestUtils;
import com.habittracker.api.auth.testutils.MockMvcTestUtils;
import com.habittracker.api.config.annotation.BaseIntegrationTest;
import com.habittracker.api.habit.dto.CreateHabitRequest;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.testutils.HabitTestUtils;
import com.habittracker.api.security.jwt.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@BaseIntegrationTest
public class HabitControllerIT {

  private static final String INVALID_UUID = "invalidId";

  @Autowired private MockMvcTestUtils mockMvcTestUtils;
  @Autowired private MockMvc mockMvc;
  @Autowired private AuthTestUtils authTestUtils;
  @Autowired private HabitTestUtils habitTestUtils;
  @Autowired private JwtService jwtService;

  private UserEntity testUser;
  private String authToken;
  private String jwtToken;

  @BeforeEach
  public void setUp() {
    testUser =
        authTestUtils.createAndSaveUser(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_TIMEZONE);
    jwtToken = jwtService.generateToken(testUser.getEmail());
    authToken = "Bearer " + jwtToken;
  }

  @Nested
  class CreateHabitTests {

    @Test
    public void shouldCreateHabit_WhenValidRequest() throws Exception {
      CreateHabitRequest request =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_READ_30_MIN);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value(HABIT_NAME_READ_DAILY))
          .andExpect(jsonPath("$.description").value(HABIT_DESCRIPTION_READ_30_MIN))
          .andExpect(jsonPath("$.frequency").value(EXPECTED_FREQUENCY))
          .andExpect(jsonPath("$.archived").value(EXPECTED_ARCHIVED))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldCreateHabitWithNullDescription_WhenDescriptionNotProvided() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest(HABIT_NAME_EXERCISE, null);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value(HABIT_NAME_EXERCISE))
          .andExpect(jsonPath("$.description").isEmpty())
          .andExpect(jsonPath("$.frequency").value(EXPECTED_FREQUENCY))
          .andExpect(jsonPath("$.archived").value(EXPECTED_ARCHIVED));
    }

    @Test
    public void shouldReturnConflict_WhenHabitNameAlreadyExists() throws Exception {
      habitTestUtils.createAndSaveHabit(testUser, HABIT_NAME_READ_DAILY);
      CreateHabitRequest request =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_ANOTHER);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(NAME_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    public void shouldReturnConflictIgnoreCase_WhenHabitNameExistsWithDifferentCase()
        throws Exception {
      habitTestUtils.createAndSaveHabit(testUser, HABIT_NAME_READ_DAILY.toLowerCase());
      CreateHabitRequest request =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY.toUpperCase(), HABIT_DESCRIPTION_GENERIC);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(NAME_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenNameIsBlank() throws Exception {
      CreateHabitRequest request = new CreateHabitRequest(BLANK_NAME, HABIT_DESCRIPTION_GENERIC);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(VALIDATION_FAILED_MESSAGE))
          .andExpect(jsonPath("$.errors.name").value(NAME_REQUIRED_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenNameTooLong() throws Exception {
      CreateHabitRequest request =
          new CreateHabitRequest(LONG_NAME_101_CHARS, HABIT_DESCRIPTION_GENERIC);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.name").value(NAME_LENGTH_MESSAGE));
    }

    @Test
    public void shouldReturnBadRequest_WhenDescriptionTooLong() throws Exception {
      CreateHabitRequest request =
          new CreateHabitRequest(HABIT_NAME_VALID, LONG_DESCRIPTION_2001_CHARS);

      mockMvcTestUtils
          .performAuthenticatedPostRequest(HABITS_ENDPOINT, request, authToken)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.description").value(DESCRIPTION_LENGTH_MESSAGE));
    }

    @Test
    public void shouldReturnUnauthorized_WhenNoAuthToken() throws Exception {
      CreateHabitRequest request =
          new CreateHabitRequest(HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_GENERIC);

      mockMvcTestUtils
          .performUnauthenticatedPostRequest(HABITS_ENDPOINT, request)
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class GetUserHabitsTests {

    @Test
    public void shouldReturnUserHabits_WhenHabitsExist() throws Exception {
      habitTestUtils.createAndSaveHabit(
          testUser, HABIT_NAME_READ_DAILY, HABIT_DESCRIPTION_READ_30_MIN);
      habitTestUtils.createAndSaveHabit(
          testUser, HABIT_NAME_EXERCISE, HABIT_DESCRIPTION_WORKOUT_45_MIN);

      mockMvcTestUtils
          .performAuthenticatedGetRequest(HABITS_ENDPOINT, authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(EXPECTED_HABIT_COUNT_2)))
          .andExpect(
              jsonPath("$[*].name", containsInAnyOrder(HABIT_NAME_READ_DAILY, HABIT_NAME_EXERCISE)))
          .andExpect(jsonPath("$[*].frequency", everyItem(is(EXPECTED_FREQUENCY))))
          .andExpect(jsonPath("$[*].archived", everyItem(is(EXPECTED_ARCHIVED))));
    }

    @Test
    public void shouldReturnEmptyList_WhenNoHabitsExist() throws Exception {
      mockMvcTestUtils
          .performAuthenticatedGetRequest(HABITS_ENDPOINT, authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(EXPECTED_HABIT_COUNT_0)));
    }

    @Test
    public void shouldOnlyReturnCurrentUserHabits_WhenMultipleUsersHaveHabits() throws Exception {
      UserEntity otherUser =
          authTestUtils.createAndSaveUser(
              OTHER_USER_EMAIL, OTHER_USER_PASSWORD, OTHER_USER_TIMEZONE);
      habitTestUtils.createAndSaveHabit(testUser, HABIT_NAME_MY_HABIT);
      habitTestUtils.createAndSaveHabit(otherUser, HABIT_NAME_OTHERS_HABIT);

      mockMvcTestUtils
          .performAuthenticatedGetRequest(HABITS_ENDPOINT, authToken)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(EXPECTED_HABIT_COUNT_1)))
          .andExpect(jsonPath("$[0].name").value(HABIT_NAME_MY_HABIT));
    }

    @Test
    public void shouldReturnUnauthorized_WhenNoAuthToken() throws Exception {
      mockMvcTestUtils
          .performUnauthenticatedGetRequest(HABITS_ENDPOINT)
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class DeleteHabitTests {

    @Test
    public void shouldReturn_NotAuthorized_WithoutAuthorization() throws Exception {
      mockMvc
          .perform(delete("/api/habits/{id}", UUID.randomUUID()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn_BadRequest_WithInvalidId() throws Exception {
      mockMvc
          .perform(addJwt(jwtToken, delete("/api/habits/{id}", INVALID_UUID)))
          .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn_NotFound_WhenHabitNotExist() throws Exception {
      mockMvc
          .perform(addJwt(jwtToken, delete("/api/habits/{id}", UUID.randomUUID())))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(HABIT_NOT_FOUND_MESSAGE));
    }

    @Test
    public void shouldReturn_NotFound_WhenHabitIsAlreadyDeleted() throws Exception {
      HabitEntity newHabit = habitTestUtils.createAndSaveHabit(testUser, "New habit");
      habitTestUtils.delete(newHabit);
      mockMvc
          .perform(addJwt(jwtToken, delete("/api/habits/{id}", newHabit.getId())))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(HABIT_ALREADY_DELETED_MESSAGE));
    }

    @Test
    public void shouldReturn_Forbidden_WhenLoggedUserInNotOwner() throws Exception {
      UserEntity anotherUser =
          authTestUtils.createAndSaveUser(
              "anotheruser@gmail.com", TEST_USER_PASSWORD, TEST_USER_TIMEZONE);
      HabitEntity newHabit = habitTestUtils.createAndSaveHabit(anotherUser, "New habit");
      mockMvc
          .perform(addJwt(jwtToken, delete("/api/habits/{id}", newHabit.getId())))
          .andExpect(status().isForbidden());
    }
  }
}
