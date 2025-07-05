package com.habittracker.api.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    Instant timestamp,
    Integer status,
    String error,
    String message,
    String path,
    Map<String, String> errors) {

  public static ApiError from(
      String message, HttpStatus status, HttpServletRequest request, Map<String, String> errors) {
    return new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        errors);
  }

  public static ApiError from(String message, HttpStatus status, HttpServletRequest request) {
    return new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        null);
  }
}
