package com.habittracker.api.security.jwt.filter;

import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.security.jwt.service.JwtService;
import com.habittracker.api.security.jwt.utils.JwtUtils;
import com.habittracker.api.security.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    JwtUtils.extractToken(request)
        .filter(jwtService::isValid)
        .flatMap(jwtService::getClaims)
        .map(this::subjectToUserDetails)
        .map(SecurityUtils::userDetailsToAuthenticationToken)
        .ifPresent(SecurityUtils::populateSecurityContext);
    filterChain.doFilter(request, response);
  }

  private UserDetails subjectToUserDetails(Claims claims) {
    return new UserDetailsImpl(
        UUID.fromString(claims.getSubject()),
        claims.get("email", String.class),
        Boolean.parseBoolean(claims.get("isAdmin", String.class)),
        parseTimeZone(claims.get("timeZone", String.class)));
  }
}
