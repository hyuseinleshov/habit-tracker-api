package com.habittracker.api.checkin.exception;

import static com.habittracker.api.checkin.constants.CheckInConstants.DUPLICATION_CHECK_IN_MESSAGE;

import java.util.UUID;

public class DuplicateCheckInException extends RuntimeException {
  public DuplicateCheckInException(UUID habitId) {
    super(String.format(DUPLICATION_CHECK_IN_MESSAGE, habitId));
  }
}
