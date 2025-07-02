package com.habittracker.api.security.utils;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public final class SecurityUtils {

  private SecurityUtils() {}

  public static void populateSecurityContext(
      AuthenticationManager authenticationManager, UsernamePasswordAuthenticationToken token)
      throws AuthenticationException {
    Authentication authenticate = authenticationManager.authenticate(token);
    SecurityContextHolder.getContext().setAuthentication(authenticate);
  }

  public static UsernamePasswordAuthenticationToken userDetailsToAuthenticationToken(
      UserDetails userDetails) {
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }
}
