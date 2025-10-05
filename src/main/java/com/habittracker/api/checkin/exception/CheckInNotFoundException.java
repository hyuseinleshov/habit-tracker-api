package com.habittracker.api.checkin.exception;

import static com.habittracker.api.checkin.constants.CheckInConstants.CHECK_IN_NOT_FOUND_MESSAGE;

import com.habittracker.api.core.exception.ResourceNotFoundException;

public class CheckInNotFoundException extends ResourceNotFoundException {
  public CheckInNotFoundException() {
    super(CHECK_IN_NOT_FOUND_MESSAGE);
  }
}
