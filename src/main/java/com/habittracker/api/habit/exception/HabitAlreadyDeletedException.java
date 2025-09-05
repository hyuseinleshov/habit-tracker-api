package com.habittracker.api.habit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_ALREADY_DELETED_MESSAGE;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HabitAlreadyDeletedException extends RuntimeException {
    public HabitAlreadyDeletedException() {
        super(HABIT_ALREADY_DELETED_MESSAGE);
    }
}
