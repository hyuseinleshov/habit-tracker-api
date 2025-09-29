package com.habittracker.api.checkin.exception;

import static com.habittracker.api.checkin.constants.CheckInConstants.DUPLICATION_CHECK_IN_MESSAGE;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCheckinException extends RuntimeException {
  public DuplicateCheckinException(UUID habitId) {
    super(String.format(DUPLICATION_CHECK_IN_MESSAGE, habitId));
  }
}
