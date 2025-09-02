package com.habittracker.api.core.exception;

import static com.habittracker.api.auth.utils.AuthConstants.MALFORMED_JSON_MESSAGE;
import static com.habittracker.api.auth.utils.AuthConstants.VALIDATION_FAILED_MESSAGE;
import static com.habittracker.api.core.exception.ExceptionConstants.*;

import com.habittracker.api.habit.exception.HabitNameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiError> handleNoHandlerFoundException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
        .body(ApiError.from(ENDPOINT_NOT_FOUND_MESSAGE, status, request));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

    String supportedMethods =
        ex.getSupportedMethods() == null
            ? ""
            : ". Supported method(s): " + String.join(", ", ex.getSupportedMethods());
    String message = ex.getMessage() + supportedMethods;
    return ResponseEntity.status(status).body(ApiError.from(message, status, request));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<ApiError> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                HashMap::new,
                (m, v) -> m.put(v.getField(), v.getDefaultMessage()),
                HashMap::putAll);

    return ResponseEntity.status(status)
        .body(ApiError.from(VALIDATION_FAILED_MESSAGE, status, request, errors));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ApiError.from(MALFORMED_JSON_MESSAGE, status, request));
  }

  @ExceptionHandler(JDBCException.class)
  public ResponseEntity<ApiError> handleConstraintViolationException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(ApiError.from(DATABASE_ERROR_MESSAGE, status, request));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }

  @ExceptionHandler(HabitNameAlreadyExistsException.class)
  public ResponseEntity<ApiError> handleHabitNameAlreadyExistsException(
      HabitNameAlreadyExistsException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgumentException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ApiError.from(ILLEGAL_ARGUMENT_MESSAGE, status, request));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(ApiError.from(INTERNAL_SERVER_ERROR_MESSAGE, status, request));
  }
}
