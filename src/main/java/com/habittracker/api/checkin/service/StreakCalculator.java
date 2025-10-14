package com.habittracker.api.checkin.service;

import com.habittracker.api.checkin.model.CheckInEntity;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StreakCalculator {

  public int calculateCurrentStreak(List<CheckInEntity> checkIns, ZoneId userTimeZone) {
    if (checkIns == null) {
      throw new IllegalArgumentException("checkIns cannot be null");
    }
    if (userTimeZone == null) {
      throw new IllegalArgumentException("userTimeZone cannot be null");
    }

    if (checkIns.isEmpty()) {
      log.debug("No check-ins found, streak is 0");
      return 0;
    }

    int streak = calculateConsecutiveDays(checkIns, userTimeZone);
    log.debug("Calculated consecutive streak: {} days", streak);
    return streak;
  }

  private int calculateConsecutiveDays(List<CheckInEntity> checkIns, ZoneId userTimeZone) {
    int streak = 1;
    LocalDate previousDate = checkIns.getFirst().getCreatedAt().atZone(userTimeZone).toLocalDate();

    for (int i = 1; i < checkIns.size(); i++) {
      LocalDate currentDate = checkIns.get(i).getCreatedAt().atZone(userTimeZone).toLocalDate();

      if (currentDate.plusDays(1).isEqual(previousDate)) {
        streak++;
        previousDate = currentDate;
      } else if (currentDate.isEqual(previousDate)) {
        log.trace("Multiple check-ins detected on date: {}", currentDate);
      } else {
        log.debug(
            "Gap detected between {} and {}, stopping streak calculation",
            currentDate,
            previousDate);
        break;
      }
    }

    return streak;
  }
}
