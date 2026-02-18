package com.habittracker.api.user.dto;

import java.util.List;

public record WeeklySummaryResponse(
    long totalHabits, long completeToday, List<DailyCheckInSummary> weekly) {}
