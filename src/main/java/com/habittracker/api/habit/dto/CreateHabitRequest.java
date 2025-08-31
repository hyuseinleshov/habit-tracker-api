package com.habittracker.api.habit.dto;

import static com.habittracker.api.habit.constants.HabitConstants.*;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateHabitRequest(
    @NotBlank(message = NAME_REQUIRED_MESSAGE) @Length(max = 100, message = NAME_LENGTH_MESSAGE) String name,
    @Length(max = 2000, message = DESCRIPTION_LENGTH_MESSAGE) String description) {}
