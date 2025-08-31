package com.habittracker.api.habit.constants;

public final class HabitTestConstants {

  // Test user data
  public static final String TEST_USER_EMAIL = "test@example.com";
  public static final String TEST_USER_PASSWORD = "password123";
  public static final String TEST_USER_TIMEZONE = "UTC";

  public static final String OTHER_USER_EMAIL = "other@example.com";
  public static final String OTHER_USER_PASSWORD = "password123";
  public static final String OTHER_USER_TIMEZONE = "UTC";

  // Test habit names
  public static final String HABIT_NAME_READ_DAILY = "Read daily";
  public static final String HABIT_NAME_EXERCISE = "Exercise";
  public static final String HABIT_NAME_MY_HABIT = "My habit";
  public static final String HABIT_NAME_OTHERS_HABIT = "Other's habit";
  public static final String HABIT_NAME_VALID = "Valid name";

  // Test habit descriptions
  public static final String HABIT_DESCRIPTION_READ_30_MIN = "Read for 30 minutes";
  public static final String HABIT_DESCRIPTION_WORKOUT_45_MIN = "Workout for 45 minutes";
  public static final String HABIT_DESCRIPTION_GENERIC = "Description";
  public static final String HABIT_DESCRIPTION_ANOTHER = "Another description";
  public static final String HABIT_DESCRIPTION_LONG = "Read for 30 minutes every day";

  // Test validation data
  public static final String BLANK_NAME = "";
  public static final String LONG_NAME_101_CHARS = "a".repeat(101);
  public static final String LONG_DESCRIPTION_2001_CHARS = "a".repeat(2001);

  // Test data variations
  public static final String HABIT_NAME_DIFFERENT_CASE = "READ DAILY";
  public static final String HABIT_NAME_WHITESPACE = "  Read daily  ";
  public static final String HABIT_DESCRIPTION_WHITESPACE = "  Description  ";

  // Expected values
  public static final String EXPECTED_FREQUENCY = "DAILY";
  public static final boolean EXPECTED_ARCHIVED = false;
  public static final int EXPECTED_HABIT_COUNT_2 = 2;
  public static final int EXPECTED_HABIT_COUNT_1 = 1;
  public static final int EXPECTED_HABIT_COUNT_0 = 0;

  // Validation error messages
  public static final String VALIDATION_FAILED_MESSAGE =
      "Validation failed for one or more fields in your request.";

  // API endpoints
  public static final String HABITS_ENDPOINT = "/api/habits";

  private HabitTestConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
