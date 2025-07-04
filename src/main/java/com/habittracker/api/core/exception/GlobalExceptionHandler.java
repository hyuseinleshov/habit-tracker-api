package com.habittracker.api.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
        .body(
            ApiError.from(
                "The requested API endpoint was not found. Please verify the URL and try again.",
                status,
                request));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ApiError> handleValidationAndHttpMessageNotReadableExceptions(
      HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ApiError.from(
                "One or more of the provided arguments are invalid. Please check the request body and parameters for errors.",
                status,
                request));
  }

  @ExceptionHandler(JDBCException.class)
  public ResponseEntity<ApiError> handleConstraintViolationException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(
            ApiError.from(
                "A database access error occurred. This may be due to connection issues, invalid SQL, or other database-related problems.",
                status,
                request));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(ApiError.from(ex.getMessage(), status, request));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgumentException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(
            ApiError.from(
                "One of the arguments provided was illegal or inappropriate for the method. Please review the input parameters.",
                status,
                request));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGenericException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
        .body(
            ApiError.from(
                "An unexpected internal server error occurred. We are working to resolve this issue. Please try again later",
                status,
                request));
  }
}
