package com.habittracker.api.habit.exception;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_ALREADY_DELETED_MESSAGE;

public class HabitAlreadyDeletedException extends RuntimeException {
  public HabitAlreadyDeletedException() {
    super(HABIT_ALREADY_DELETED_MESSAGE);
  }
}
