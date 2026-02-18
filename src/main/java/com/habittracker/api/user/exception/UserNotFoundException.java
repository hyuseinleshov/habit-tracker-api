package com.habittracker.api.user.exception;

import static com.habittracker.api.user.constants.UserProfileConstants.USER_NOT_FOUND_MESSAGE;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super(USER_NOT_FOUND_MESSAGE);
  }
}
