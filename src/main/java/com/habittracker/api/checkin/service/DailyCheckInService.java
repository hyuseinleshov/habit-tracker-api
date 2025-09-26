package com.habittracker.api.checkin.service;

import com.habittracker.api.habit.model.HabitEntity;
import java.time.ZoneId;

public interface DailyCheckInService {

  void registerTodayCheckin(HabitEntity habit, ZoneId userTimeZone);
}
