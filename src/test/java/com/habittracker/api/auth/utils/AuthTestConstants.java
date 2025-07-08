package com.habittracker.api.auth.utils;

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

  // API endpoints
  public static final String REGISTER_ENDPOINT = "/api/auth/register";
  public static final String LOGIN_ENDPOINT = "/api/auth/login";

  // Response messages
  public static final String REGISTER_SUCCESS_MESSAGE = "Register successful";
  public static final String LOGIN_SUCCESS_MESSAGE = "Login successful";
  public static final String VALIDATION_FAILED_MESSAGE =
      "Validation failed for one or more fields in your request.";

  // Validation error messages
  public static final String EMAIL_REQUIRED_MESSAGE = "Email is required";
  public static final String INVALID_EMAIL_MESSAGE = "Email must be valid";
  public static final String PASSWORD_REQUIRED_MESSAGE = "Password is required";
  public static final String PASSWORD_LENGTH_MESSAGE = "Password must be at least 6 characters";

  // Exception messages
  public static final String EMAIL_EXISTS_MESSAGE = "Email already exists";
  public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password";
  public static final String INVALID_CREDENTIALS_ERROR = "Invalid email or password";

  private AuthTestConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
