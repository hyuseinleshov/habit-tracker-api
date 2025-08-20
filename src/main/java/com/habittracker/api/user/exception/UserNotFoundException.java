package com.habittracker.api.user.exception;

import static com.habittracker.api.user.constants.UserProfileConstants.USER_NOT_FOUND_MESSAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super(USER_NOT_FOUND_MESSAGE);
  }
}
