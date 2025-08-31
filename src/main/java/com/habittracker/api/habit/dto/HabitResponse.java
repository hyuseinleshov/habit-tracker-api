package com.habittracker.api.habit.dto;

import com.habittracker.api.habit.model.Frequency;
import java.util.UUID;

public record HabitResponse(
    UUID id, String name, String description, Frequency frequency, boolean archived) {}
