package com.habittracker.api.user.dto;

import java.time.LocalDate;

public record DailyCheckinSummary(LocalDate date, long checkins) {}
