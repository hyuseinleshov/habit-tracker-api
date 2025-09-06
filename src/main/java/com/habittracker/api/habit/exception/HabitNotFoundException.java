package com.habittracker.api.habit.exception;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HabitNotFoundException extends RuntimeException {
  public HabitNotFoundException() {
    super(HABIT_NOT_FOUND_MESSAGE);
  }
}
