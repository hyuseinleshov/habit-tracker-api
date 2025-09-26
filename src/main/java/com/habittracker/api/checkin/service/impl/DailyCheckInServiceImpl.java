package com.habittracker.api.checkin.service.impl;

import com.habittracker.api.checkin.exception.DuplicateCheckinException;
import com.habittracker.api.checkin.service.DailyCheckInService;
import com.habittracker.api.core.utils.TimezoneUtils;
import com.habittracker.api.habit.model.HabitEntity;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyCheckInServiceImpl implements DailyCheckInService {

  private final StringRedisTemplate redisTemplate;

  @Override
  public void registerTodayCheckin(HabitEntity habit, ZoneId userTimeZone) {
    String key = "check-in" + habit.getId();
    if (redisTemplate.hasKey(key)) {
      throw new DuplicateCheckinException(habit.getId());
    }
    redisTemplate
        .opsForValue()
        .set(key, "1", TimezoneUtils.calculateDurationUntilMidnight(userTimeZone));
  }
}
