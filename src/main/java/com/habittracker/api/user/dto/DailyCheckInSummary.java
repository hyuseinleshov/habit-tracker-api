package com.habittracker.api.user.dto;

import java.time.LocalDate;

public record DailyCheckInSummary(LocalDate date, long checkins) {}
