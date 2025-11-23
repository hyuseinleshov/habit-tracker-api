package com.habittracker.api.checkin.service;

import com.habittracker.api.habit.model.HabitEntity;
import java.time.ZoneId;
import java.util.UUID;

public interface DailyCheckInService {

  void registerTodayCheckin(HabitEntity habit, UUID userId, ZoneId userTimeZone);

  boolean isCheckedInToday(UUID habitId);
}
