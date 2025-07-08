package com.habittracker.api.auth.exception;

import com.habittracker.api.core.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentialsException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(status)
        .body(ApiError.from("Invalid username or password", status, request));
  }
}
