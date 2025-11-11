package com.habittracker.api.habit.streak.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BestStreakData(int days, LocalDate startDate, LocalDate endDate, UUID habitId) {}
