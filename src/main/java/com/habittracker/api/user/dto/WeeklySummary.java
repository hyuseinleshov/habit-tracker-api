package com.habittracker.api.user.dto;

import java.util.List;

public record WeeklySummary(long totalHabits, long completeToday, List<DailyCheckinSummary> weekly) {
}
