package com.habittracker.api.security.jwt.service;

import com.habittracker.api.security.jwt.config.JwtProperties;
import com.habittracker.api.security.jwt.utils.JwtTestUtils;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {


    private static final String DUMMY_EMAIL = "subject@gmail.com";
    private static final String DUMMY_ISSUER = "test-issuer";
    private static final Duration DUMMY_EXPIRATION_DURATION = Duration.of(15, ChronoUnit.HOURS);
    private static final Duration DUMMY_NOT_BEFORE_LEEWAY_DURATION = Duration.of(7, ChronoUnit.MINUTES);
    private static final long TOLERANCE_SECONDS = 1;

    private final SecretKey testKey = Jwts.SIG.HS256.key().build();
    private final JwtTestUtils utils = new JwtTestUtils(testKey);

    @Mock
    private JwtProperties jwtProperties;

    private JwtServiceImpl totest;

    @BeforeEach
    void setUp() {
        totest = new JwtServiceImpl(testKey, jwtProperties);
    }

    private void setUpJwtProperties() {
        when(jwtProperties.getIssuer()).thenReturn(DUMMY_ISSUER);
        when(jwtProperties.getExpirationDuration()).thenReturn(DUMMY_EXPIRATION_DURATION);
        when(jwtProperties.getNotBeforeLeewayDuration()).thenReturn(DUMMY_NOT_BEFORE_LEEWAY_DURATION);
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

            totest.generateToken(DUMMY_EMAIL);

            Instant now = Instant.now();

            verify(spyBuilder).subject(DUMMY_EMAIL);
            verify(spyBuilder).issuer(DUMMY_ISSUER);
            verify(spyBuilder).issuedAt(dateCaptor.capture());
            verify(spyBuilder).expiration(dateCaptor.capture());
            verify(spyBuilder).notBefore(dateCaptor.capture());
            verify(spyBuilder).signWith(testKey);


            List<Instant> captureTimes = dateCaptor.getAllValues().stream().map(Date::toInstant).toList();
            Instant issueAt = captureTimes.get(0);
            Instant expiration = captureTimes.get(1);
            Instant notBefore = captureTimes.get(2);

            assertTrue(Duration.between(now, issueAt).abs().toSeconds() <= TOLERANCE_SECONDS);
            assertTrue(Duration.between(now, expiration).minus(DUMMY_EXPIRATION_DURATION).abs().toSeconds() <= TOLERANCE_SECONDS);
            assertTrue(Duration.between(now, notBefore).plus(DUMMY_NOT_BEFORE_LEEWAY_DURATION).abs().toSeconds() <= TOLERANCE_SECONDS);
        }
    }

    @Test
    public void isValid_shouldReturnFalse_whenMalformedJwtExceptionThrown() {
        String mailFormedJwt = "mailFormedJwt";
        assertFalse(totest.isValid(mailFormedJwt));
    }

    @Test
    public void isValid_shouldReturnFalse_whenSignatureExceptionThrown() {
        String tokenWithWrongSignature = utils.tokenWithInvalidSignature();
        assertFalse(totest.isValid(tokenWithWrongSignature));
    }

    @Test
    public void isValid_shouldReturnFalse_whenExpiredJwtExceptionThrown() {
        Instant now = Instant.now();
        String expiredToken = utils.generateTestToken(DUMMY_EMAIL,
                DUMMY_ISSUER,
                now.minus(2, ChronoUnit.MINUTES),
                now);
        assertFalse(totest.isValid(expiredToken));
    }

    @Test
    public void isValid_shouldReturnFalse_whenPrematureJwtExceptionThrown() {
        Instant now = Instant.now();
        String prematureToken =  utils.generateTestToken(DUMMY_EMAIL,
                DUMMY_ISSUER,
                now.plus(20, ChronoUnit.MINUTES),
                now.plus(5, ChronoUnit.MINUTES));
        assertFalse(totest.isValid(prematureToken));
    }

    @Test
    public void isValid_shouldReturnFalse_whenIncorrectClaimExceptionThrown() {
        when(jwtProperties.getIssuer()).thenReturn(DUMMY_ISSUER);
        Instant now = Instant.now();
        String incorrectClaimToken =  utils.generateTestToken(DUMMY_EMAIL,
                "fake-issuer",
                now.plus(20, ChronoUnit.MINUTES),
                now.minus(5, ChronoUnit.SECONDS));
        assertFalse(totest.isValid(incorrectClaimToken));
    }

    @Test
    public void isValid_shouldReturnFalse_whenIllegalArgumentExceptionThrown() {
        assertFalse(totest.isValid(""));
        assertFalse(totest.isValid(null));
    }

    @Test
    public void isValid_shouldReturnTrue_whenTokenIsValid() {
        String validToken = utils.generateTestToken(DUMMY_EMAIL,
                DUMMY_ISSUER);
        assertTrue(totest.isValid(validToken));
    }
}
