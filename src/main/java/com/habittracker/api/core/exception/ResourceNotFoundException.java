package com.habittracker.api.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public abstract class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException() {
    super(
        "The specific resource you requested could not be found. It may have been moved, deleted, or never existed.");
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
