package com.habittracker.api.habit.exception;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;

public class HabitNotFoundException extends RuntimeException {
  public HabitNotFoundException() {
    super(HABIT_NOT_FOUND_MESSAGE);
  }
}
