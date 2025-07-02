package com.habittracker.api.security.jwt.filter;

import com.habittracker.api.security.jwt.service.JwtService;
import com.habittracker.api.security.jwt.utils.JwtUtils;
import com.habittracker.api.security.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final AuthenticationManager authenticationManager;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    JwtUtils.extractToken(request)
        .filter(jwtService::isValid)
        .flatMap(jwtService::extractSubject)
        .flatMap(this::subjectToUserDetails)
        .map(SecurityUtils::userDetailsToAuthenticationToken)
        .ifPresent(this::populateSecurityContext);
    filterChain.doFilter(request, response);
  }

  private Optional<UserDetails> subjectToUserDetails(String subject) {
    try {
      return Optional.of(userDetailsService.loadUserByUsername(subject));
    } catch (UsernameNotFoundException e) {
      return Optional.empty();
    }
  }

  private void populateSecurityContext(UsernamePasswordAuthenticationToken token)
      throws AuthenticationException {
    try {
      Authentication authenticate = authenticationManager.authenticate(token);
      SecurityContextHolder.getContext().setAuthentication(authenticate);
    } catch (AuthenticationException ex) {
      log.warn("Authentication failed: invalid credentials");
      log.debug("Authentication error details: {}", ex.getMessage());
    }
  }
}
