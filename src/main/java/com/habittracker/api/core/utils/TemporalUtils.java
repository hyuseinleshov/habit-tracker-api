package com.habittracker.api.core.utils;

import java.time.LocalDate;
import java.time.ZoneId;

public final class TemporalUtils {

  private TemporalUtils() {}

  public static boolean isTodayOrYesterday(LocalDate date, ZoneId timeZone) {
    LocalDate today = LocalDate.now(timeZone);
    return !date.isBefore(today.minusDays(1));
  }
}
