package com.habittracker.api.checkin.constants;

public final class CheckInConstants {

  private CheckInConstants() {}

  public static final String DUPLICATION_CHECK_IN_MESSAGE =
      "A check-in for habit with id %s already exists for today.";
  public static final String CHECK_IN_NOT_FOUND_MESSAGE = "Check-in not found.";
}
