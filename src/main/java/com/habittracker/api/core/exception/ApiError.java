package com.habittracker.api.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiError(Instant timestamp, Integer status, String error, String message,
                       String path) {

    public static ApiError from(String message, HttpStatus status, HttpServletRequest request) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }
}
