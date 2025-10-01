package com.habittracker.api.checkin.dto;

import com.habittracker.api.habit.dto.HabitResponse;
import java.time.Instant;
import java.util.UUID;

public record CheckInWithHabitResponse(UUID id, HabitResponse habit, Instant createdAt) {}
