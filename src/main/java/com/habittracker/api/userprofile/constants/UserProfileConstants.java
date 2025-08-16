package com.habittracker.api.userprofile.constants;

public final class UserProfileConstants {

  private UserProfileConstants() {}

  public static final String INVALID_FIRST_NAME_MESSAGE =
      "First name must be at most 50 characters";
  public static final String INVALID_LAST_NAME_MESSAGE = "Last name must be at most 50 characters";
  public static final String INVALID_AGE_MESSAGE = "Age must be between 0 and 150";
  public static final String INVALID_TIMEZONE_MESSAGE = "Invalid timezone";
  public static final String USER_CANT_BE_NULL_MESSAGE = "User can't be null";
  public static final String USER_PROFILE_NOT_FOUND_MESSAGE = "User profile is not found";
  public static final String USER_PROFILE_DATA_NOT_VALID_MESSAGE = "Invalid user profile data";
}
