package com.habittracker.api.userprofile.exception;

import static com.habittracker.api.userprofile.constants.UserProfileConstants.USER_PROFILE_NOT_FOUND_MESSAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(NOT_FOUND)
public class UserProfileNotFoundException extends RuntimeException {
  public UserProfileNotFoundException() {
    super(USER_PROFILE_NOT_FOUND_MESSAGE);
  }
}
