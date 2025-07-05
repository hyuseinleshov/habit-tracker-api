package com.habittracker.api.security.handlers;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.core.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper mapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    response.setStatus(SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApiError message =
        ApiError.from(
            "Permission denied. Your current role does not grant access to this functionality.",
            HttpStatus.FORBIDDEN,
            request);
    mapper.writeValue(response.getWriter(), message);
  }
}
