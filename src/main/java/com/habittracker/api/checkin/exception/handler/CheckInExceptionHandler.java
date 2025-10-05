package com.habittracker.api.checkin.exception.handler;

import com.habittracker.api.checkin.exception.CheckInNotFoundException;
import com.habittracker.api.checkin.exception.DuplicateCheckinException;
import com.habittracker.api.core.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CheckInExceptionHandler {

  @ExceptionHandler(DuplicateCheckinException.class)
  public ResponseEntity<ApiError> handleDuplicateCheckInException(
      DuplicateCheckinException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }

  @ExceptionHandler(CheckInNotFoundException.class)
  public ResponseEntity<ApiError> handleCheckInNotFoundException(
      CheckInNotFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }
}
