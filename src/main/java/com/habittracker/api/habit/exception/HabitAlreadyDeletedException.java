package com.habittracker.api.habit.exception;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_ALREADY_DELETED_MESSAGE;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HabitAlreadyDeletedException extends RuntimeException {
  public HabitAlreadyDeletedException() {
    super(HABIT_ALREADY_DELETED_MESSAGE);
  }
}
