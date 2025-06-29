package com.habittracker.api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private HttpServletRequest request;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DUMMY_JWT_TOKEN = "Test jwt token";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Test
    void shouldReturnEmptyWhenAuthorizationHeaderIsMissing() {
        Optional<String> token = JwtUtils.extractToken(request);
        assertTrue(token.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenAuthorizationHeaderHasWrongFormat() {
        when(request.getHeader(AUTHORIZATION_HEADER))
                .thenReturn("Authorization: token");
        Optional<String> token = JwtUtils.extractToken(request);
        assertTrue(token.isEmpty());
    }

    @Test
    void shouldExtractTokenWhenAuthorizationHeaderIsCorrect() {
        when(request.getHeader(AUTHORIZATION_HEADER))
                .thenReturn(BEARER_PREFIX + DUMMY_JWT_TOKEN);
        Optional<String> tokenOptional = JwtUtils.extractToken(request);
        assertTrue(tokenOptional.isPresent());
        String token = tokenOptional.get();
        assertEquals(DUMMY_JWT_TOKEN, token);
    }
}