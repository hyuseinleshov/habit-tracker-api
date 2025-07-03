package com.habittracker.api.security.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public final class SecurityUtils {

  private SecurityUtils() {}

  public static UsernamePasswordAuthenticationToken userDetailsToAuthenticationToken(
      UserDetails userDetails) {
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  public static void populateSecurityContext(UsernamePasswordAuthenticationToken token) {
    SecurityContextHolder.getContext().setAuthentication(token);
  }
}
