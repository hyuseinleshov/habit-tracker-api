package com.habittracker.api.security.jwt.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.habittracker.api.config.BaseIntegrationTest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtPropertiesIT extends BaseIntegrationTest {

  @Autowired private JwtProperties jwtProperties;

  private static final String TEST_JWT_SECRET = "9NuCXLE3rpz8/lPYeIag9xs7wyRHKat8exvUmlm01S8=";
  private static final String TEST_JWT_ISSUER = "test-issuer";
  private static final Duration TEST_JWT_EXPIRATION_DURATION = Duration.of(1, ChronoUnit.MINUTES);
  private static final Duration TEST_JWT_NOT_BEFORE_LEEWAY_DURATION =
      Duration.of(20, ChronoUnit.MINUTES);
  private static final Integer TEST_JWT_CLOCK_SKEW_SECONDS = 50;

  @Test
  void jwtPropertiesAreLoadedSuccessfully() {
    assertEquals(TEST_JWT_SECRET, jwtProperties.getSecret());
    assertEquals(TEST_JWT_ISSUER, jwtProperties.getIssuer());
    assertEquals(TEST_JWT_EXPIRATION_DURATION, jwtProperties.getExpirationDuration());
    assertEquals(TEST_JWT_NOT_BEFORE_LEEWAY_DURATION, jwtProperties.getNotBeforeLeewayDuration());
    assertEquals(TEST_JWT_CLOCK_SKEW_SECONDS, jwtProperties.getClockSkewSeconds());
  }
}
