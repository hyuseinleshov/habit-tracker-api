package com.habittracker.api.security.jwt.service;

import static com.habittracker.api.security.jwt.utils.JwtTestConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.habittracker.api.security.jwt.config.JwtProperties;
import com.habittracker.api.security.jwt.utils.JwtTestUtils;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

  private static final Duration EXPIRATION_DURATION = Duration.of(15, ChronoUnit.HOURS);
  private static final Duration NOT_BEFORE_LEEWAY_DURATION = Duration.of(7, ChronoUnit.MINUTES);
  private static final long TOLERANCE_SECONDS = 1;

  @Mock private JwtProperties jwtProperties;

  private JwtServiceImpl totest;

  @BeforeEach
  void setUp() {
    totest = new JwtServiceImpl(TEST_SECRET_KEY, jwtProperties);
  }

  private void setUpJwtProperties() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getExpirationDuration()).thenReturn(EXPIRATION_DURATION);
    when(jwtProperties.getNotBeforeLeewayDuration()).thenReturn(NOT_BEFORE_LEEWAY_DURATION);
  }

  @Test
  public void generateToken_shouldBuildCorrectTokenWithTimestampsAndClaims() {
    final JwtBuilder spyBuilder = spy(Jwts.builder());
    final JwtBuilder.BuilderHeader builderHeader = mock(JwtBuilder.BuilderHeader.class);
    final ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
    setUpJwtProperties();

    try (MockedStatic<Jwts> jwtsMockedStatic = mockStatic(Jwts.class)) {
      jwtsMockedStatic.when(Jwts::builder).thenReturn(spyBuilder);

      doReturn(builderHeader).when(spyBuilder).header();
      when(builderHeader.and()).thenReturn(spyBuilder);
      when(builderHeader.type(anyString())).thenReturn(builderHeader);

      doReturn(spyBuilder).when(spyBuilder).subject(any());
      doReturn(spyBuilder).when(spyBuilder).issuer(any());
      doReturn(spyBuilder).when(spyBuilder).expiration(any());
      doReturn(spyBuilder).when(spyBuilder).notBefore(any());
      doReturn(spyBuilder).when(spyBuilder).issuedAt(any());

      totest.generateToken(TEST_SUBJECT);

      Instant now = Instant.now();

      verify(spyBuilder).subject(TEST_SUBJECT);
      verify(spyBuilder).issuer(TEST_ISSUER);
      verify(spyBuilder).issuedAt(dateCaptor.capture());
      verify(spyBuilder).expiration(dateCaptor.capture());
      verify(spyBuilder).notBefore(dateCaptor.capture());
      verify(spyBuilder).signWith(TEST_SECRET_KEY);

      List<Instant> captureTimes = dateCaptor.getAllValues().stream().map(Date::toInstant).toList();
      Instant issueAt = captureTimes.get(0);
      Instant expiration = captureTimes.get(1);
      Instant notBefore = captureTimes.get(2);

      assertTrue(Duration.between(now, issueAt).abs().toSeconds() <= TOLERANCE_SECONDS);
      assertTrue(
          Duration.between(now, expiration).minus(EXPIRATION_DURATION).abs().toSeconds()
              <= TOLERANCE_SECONDS);
      assertTrue(
          Duration.between(now, notBefore).plus(NOT_BEFORE_LEEWAY_DURATION).abs().toSeconds()
              <= TOLERANCE_SECONDS);
    }
  }

  @ParameterizedTest
  @MethodSource("com.habittracker.api.security.jwt.utils.JwtTestUtils#getInvalidTokens")
  public void isValid_shouldReturnExpectedResult_forGivenTokens(String token) {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    assertFalse(totest.isValid(token));
  }

  @Test
  public void isValid_shouldReturnTrue_whenTokenIsValid() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    String validToken = JwtTestUtils.generateValidToken();
    assertTrue(totest.isValid(validToken));
  }

  @ParameterizedTest
  @MethodSource("com.habittracker.api.security.jwt.utils.JwtTestUtils#getInvalidTokens")
  public void extractSubject_shouldReturnEmpty_forGivenTokens(String token) {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    assertTrue(totest.extractSubject(token).isEmpty());
  }

  @Test
  public void extractSubject_shouldReturnSubject_whenTokenIsValid() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    String token = JwtTestUtils.generateValidToken();
    Optional<String> subjectOptional = totest.extractSubject(token);
    assertTrue(subjectOptional.isPresent());
    String subject = subjectOptional.get();
    assertEquals(TEST_SUBJECT, subject);
  }
}
