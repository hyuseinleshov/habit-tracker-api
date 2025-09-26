package com.habittracker.api.core.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public final class TimezoneUtils {

  private TimezoneUtils() {}

  private static final Set<String> VALID_ZONE_IDS = ZoneId.getAvailableZoneIds();

  public static boolean isValidTimezone(String timezone) {
    if (timezone == null) return false;
    return VALID_ZONE_IDS.contains(timezone.trim());
  }

  public static Duration calculateDurationUntilMidnight(ZoneId userTimeZone) {
    LocalDateTime now = LocalDateTime.now(userTimeZone);
    LocalDateTime midnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
    System.out.println(now.truncatedTo(ChronoUnit.MINUTES));
    return Duration.between(now, midnight);
  }
}
