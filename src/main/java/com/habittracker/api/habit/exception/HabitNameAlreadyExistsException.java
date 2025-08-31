package com.habittracker.api.habit.exception;

import static com.habittracker.api.habit.constants.HabitConstants.NAME_ALREADY_EXISTS_MESSAGE;

public class HabitNameAlreadyExistsException extends RuntimeException {

  public HabitNameAlreadyExistsException() {
    super(NAME_ALREADY_EXISTS_MESSAGE);
  }
}
