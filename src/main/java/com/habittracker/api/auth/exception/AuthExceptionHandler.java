package com.habittracker.api.auth.exception;

import static com.habittracker.api.auth.utils.AuthConstants.INVALID_CREDENTIALS_MESSAGE;
import static com.habittracker.api.auth.utils.AuthConstants.MISSING_REFRESH_TOKEN_MESSAGE;

import com.habittracker.api.core.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentialsException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(status)
        .body(ApiError.from(INVALID_CREDENTIALS_MESSAGE, status, request));
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ApiError> handleMissingRequestCookieException(HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ApiError.from(MISSING_REFRESH_TOKEN_MESSAGE, status, request));
  }
}
