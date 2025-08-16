package com.habittracker.api.security.jwt.utils;

import static com.habittracker.api.config.constants.JwtTestConstant.AUTHORIZATION_HEADER;
import static com.habittracker.api.config.constants.JwtTestConstant.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  @Mock private HttpServletRequest request;

  private static final String DUMMY_JWT_TOKEN = "Test jwt token";

  @Test
  void shouldReturnEmptyWhenAuthorizationHeaderIsMissing() {
    Optional<String> token = JwtUtils.extractToken(request);
    assertTrue(token.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenAuthorizationHeaderHasWrongFormat() {
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Authorization: token");
    Optional<String> token = JwtUtils.extractToken(request);
    assertTrue(token.isEmpty());
  }

  @Test
  void shouldExtractTokenWhenAuthorizationHeaderIsCorrect() {
    when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + DUMMY_JWT_TOKEN);
    Optional<String> tokenOptional = JwtUtils.extractToken(request);
    assertTrue(tokenOptional.isPresent());
    String token = tokenOptional.get();
    assertEquals(DUMMY_JWT_TOKEN, token);
  }
}
