package com.habittracker.api.security.exception;

import com.habittracker.api.security.handlers.CustomAccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodSecurityExceptionHandler {

    private final CustomAccessDeniedHandler accessDeniedHandler;

    @ExceptionHandler(AuthorizationDeniedException.class)
    public void handleAuthorizationDenied(HttpServletRequest request,
                                          HttpServletResponse response,
                                          AuthorizationDeniedException ex) throws IOException {
        accessDeniedHandler.handle(request, response, ex);
    }
}
