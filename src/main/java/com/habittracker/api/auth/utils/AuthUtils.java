package com.habittracker.api.auth.utils;

import static com.habittracker.api.core.exception.ExceptionConstants.ILLEGAL_STATE_MESSAGE;
import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.auth.model.UserDetailsImpl;
import java.time.ZoneId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtils {

  public static ZoneId getUserTimeZone() {
    return parseTimeZone(getDetails().getProfile().getTimezone());
  }

  private static UserDetailsImpl getDetails() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
      throw new IllegalStateException(ILLEGAL_STATE_MESSAGE);
    }
    return (UserDetailsImpl) authentication.getPrincipal();
  }
}
