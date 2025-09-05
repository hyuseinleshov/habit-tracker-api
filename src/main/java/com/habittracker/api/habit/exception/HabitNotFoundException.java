package com.habittracker.api.habit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_FOUND_MESSAGE;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException() {
        super(HABIT_NOT_FOUND_MESSAGE);
    }
}
