package com.habittracker.api.auth.utils;

public final class AuthConstants {

  // API endpoints
  public static final String REGISTER_ENDPOINT = "/api/auth/register";
  public static final String LOGIN_ENDPOINT = "/api/auth/login";
  public static final String REFRESH_ENDPOINT = "/api/auth/refresh";
  public static final String LOGOUT_ENDPOINT = "/api/auth/logout";

  // Response messages
  public static final String REGISTER_SUCCESS_MESSAGE = "Register successful";
  public static final String LOGIN_SUCCESS_MESSAGE = "Login successful";
  public static final String REFRESH_SUCCESS_MESSAGE = "Token refreshed successfully";
  public static final String LOGOUT_SUCCESS_MESSAGE = "Logout successful";

  // Exception and error messages
  public static final String EMAIL_EXISTS_MESSAGE = "Email already exists";
  public static final String EMAIL_DELETED_MESSAGE =
      "This email was previously used by a deleted account. You can register with it again after %s";
  public static final String INVALID_CREDENTIALS_ERROR = "Invalid email or password";
  public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password";
  public static final String VALIDATION_FAILED_MESSAGE =
      "Validation failed for one or more fields in your request.";
  public static final String MALFORMED_JSON_MESSAGE = "Request body is missing or malformed.";
  public static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token";
  public static final String MISSING_REFRESH_TOKEN_MESSAGE = "Refresh token cookie is missing";

  // Validation messages
  public static final String EMAIL_REQUIRED_MESSAGE = "Email is required";
  public static final String INVALID_EMAIL_MESSAGE = "Email must be valid";
  public static final String PASSWORD_REQUIRED_MESSAGE = "Password is required";
  public static final String PASSWORD_LENGTH_MESSAGE = "Password must be at least 6 characters";

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  private AuthConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
