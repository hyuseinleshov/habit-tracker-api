package com.habittracker.api.config.constants;

public final class AuthTestConstants {

  // Common test data
  public static final String TEST_EMAIL = "user@example.com";
  public static final String TEST_PASSWORD = "password123";
  public static final String ENCODED_PASSWORD = "encodedPassword";
  public static final String EXISTING_EMAIL = "existing@example.com";
  public static final String NEW_USER_EMAIL = "newuser@example.com";
  public static final String NONEXISTENT_EMAIL = "nonexistent@example.com";
  public static final String WRONG_PASSWORD = "wrongpassword";
  public static final String JWT_TOKEN = "jwt-token";
  public static final String REFRESH_TOKEN = "dummy-refresh-token";
  public static final String NEW_ACCESS_TOKEN = "new-access-token";
  public static final String NEW_REFRESH_TOKEN = "new-refresh-token";

  // JSON Strings
  public static final String EMPTY_JSON = "{}";
  public static final String MALFORMED_JSON = "{\"email\": \"test@example.com\", \"password\":}";

  private AuthTestConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
