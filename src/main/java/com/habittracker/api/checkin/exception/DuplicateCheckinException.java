package com.habittracker.api.checkin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import static com.habittracker.api.checkin.constants.CheckInConstants.DUPLICATION_CHECK_IN_MESSAGE;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCheckinException extends RuntimeException {
  public DuplicateCheckinException(UUID habitId) {
    super(String.format(DUPLICATION_CHECK_IN_MESSAGE, habitId));
  }
}
