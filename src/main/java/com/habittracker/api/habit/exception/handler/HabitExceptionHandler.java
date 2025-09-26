package com.habittracker.api.habit.exception.handler;

import com.habittracker.api.core.exception.ApiError;
import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import com.habittracker.api.habit.exception.HabitNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(-1)
public class HabitExceptionHandler {

  @ExceptionHandler(HabitNameAlreadyExistsException.class)
  public ResponseEntity<ApiError> handleHabitNameAlreadyExistsException(
      HabitNameAlreadyExistsException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }

  @ExceptionHandler(value = {HabitNotFoundException.class, HabitAlreadyDeletedException.class})
  public ResponseEntity<ApiError> handleHabitMissingExceptions(
      Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }
}
