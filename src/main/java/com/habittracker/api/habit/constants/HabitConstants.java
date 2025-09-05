package com.habittracker.api.habit.constants;

public final class HabitConstants {

  // Validation messages
  public static final String NAME_REQUIRED_MESSAGE = "Habit name is required";
  public static final String NAME_LENGTH_MESSAGE =
      "Habit name must be between 1 and 100 characters";
  public static final String DESCRIPTION_LENGTH_MESSAGE =
      "Habit description must not exceed 2000 characters";
  public static final String NAME_ALREADY_EXISTS_MESSAGE = "A habit with this name already exists";
  public static final String HABIT_NOT_FOUND_MESSAGE = "Habit not found.";
  public static final String HABIT_ALREADY_DELETED_MESSAGE = "This habit has already been deleted.";

  private HabitConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
