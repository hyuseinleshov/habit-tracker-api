package com.habittracker.api.auth.utils;

import static com.habittracker.api.core.utils.TimeZoneUtils.parseTimeZone;

import com.habittracker.api.auth.model.UserDetailsImpl;
import java.time.ZoneId;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtils {

  public static ZoneId getUserTimeZone() {
    return parseTimeZone(getDetails().getProfile().getTimezone());
  }

  private static UserDetailsImpl getDetails() {
    return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
