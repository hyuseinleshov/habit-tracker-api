package com.habittracker.api.core.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public final class TimeZoneUtils {

  private TimeZoneUtils() {}

  private static final Set<String> VALID_ZONE_IDS = ZoneId.getAvailableZoneIds();
  public static final String INVALID_TIMEZONE_MESSAGE = "Specified timezone is not valid.";

  public static boolean isValidTimeZone(String timezone) {
    if (timezone == null) return false;
    return VALID_ZONE_IDS.contains(timezone.trim());
  }

  public static ZoneId parseTimeZone(String timeZone) {
    if (!isValidTimeZone(timeZone)) {
      throw new IllegalArgumentException(INVALID_TIMEZONE_MESSAGE);
    }
    return ZoneId.of(timeZone);
  }

  public static Duration calculateDurationUntilMidnight(ZoneId userTimeZone) {
    LocalDateTime now = LocalDateTime.now(userTimeZone);
    LocalDateTime midnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
    return Duration.between(now, midnight);
  }
}
