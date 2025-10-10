package com.habittracker.api.checkin.dto;

import java.time.LocalDate;

public record StreakCalculationResult(int streak, LocalDate mostRecentCheckInDate) {}
