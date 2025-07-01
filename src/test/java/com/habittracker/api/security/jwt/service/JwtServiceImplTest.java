package com.habittracker.api.security.jwt.service;

import com.habittracker.api.security.jwt.config.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {


    private static final String DUMMY_EMAIL = "subject@gmail.com";
    private static final String DUMMY_ISSUER = "issuer";
    private static final Duration DUMMY_EXPIRATION_DURATION = Duration.of(15, ChronoUnit.HOURS);
    private static final Duration DUMMY_NOT_BEFORE_LEEWAY_DURATION = Duration.of(7, ChronoUnit.MINUTES);
    private static final long TOLERANCE_SECONDS = 1;

    private final SecretKey testKey = Jwts.SIG.HS256.key().build();

    @Captor
    private ArgumentCaptor<Date> dateCaptor;

    @Mock
    private JwtProperties jwtProperties;

    @Spy
    private JwtBuilder spyBuilder = Jwts.builder();

    @Mock
    private JwtBuilder.BuilderHeader builderHeader;

    private JwtServiceImpl totest;

    @BeforeEach
    void setUp() {
        setUpJwtProperties();
        totest = new JwtServiceImpl(testKey, jwtProperties);
    }

    private void setUpJwtProperties() {
        when(jwtProperties.getIssuer()).thenReturn(DUMMY_ISSUER);
        when(jwtProperties.getExpirationDuration()).thenReturn(DUMMY_EXPIRATION_DURATION);
        when(jwtProperties.getNotBeforeLeewayDuration()).thenReturn(DUMMY_NOT_BEFORE_LEEWAY_DURATION);
    }

    @Test
    public void generateToken_shouldBuildCorrectTokenWithTimestampsAndClaims() {


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
}
