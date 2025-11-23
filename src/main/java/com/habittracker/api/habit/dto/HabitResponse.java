package com.habittracker.api.habit.dto;

import com.habittracker.api.habit.model.Frequency;
import java.time.Instant;
import java.util.UUID;

public record HabitResponse(
    UUID id,
    String name,
    String description,
    Frequency frequency,
    Instant createdAt,
    boolean checkedInToday,
    int currentStreak,
    boolean archived) {}
