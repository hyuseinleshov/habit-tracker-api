package com.habittracker.api.habit.dto;

import java.util.UUID;
import com.habittracker.api.habit.model.Frequency;

public record HabitResponse(
        UUID id,
        String name,
        String description,
        Frequency frequency,
        boolean archived) {
}
