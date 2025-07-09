package com.habittracker.api.core.exception;

import static com.habittracker.api.core.exception.ExceptionConstants.RESOURCE_NOT_FOUND_MESSAGE;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public abstract class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException() {
    super(RESOURCE_NOT_FOUND_MESSAGE);
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
