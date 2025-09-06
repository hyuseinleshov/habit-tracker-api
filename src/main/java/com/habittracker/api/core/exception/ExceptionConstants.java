package com.habittracker.api.core.exception;

public final class ExceptionConstants {

  // HTTP 404 - Not Found
  public static final String ENDPOINT_NOT_FOUND_MESSAGE =
      "The requested API endpoint was not found. Please verify the URL and try again.";
  public static final String RESOURCE_NOT_FOUND_MESSAGE = "The requested resource was not found.";

  // HTTP 400 - Bad Request
  public static final String ILLEGAL_ARGUMENT_MESSAGE =
      "One of the arguments provided was illegal or inappropriate for the method. Please review the input parameters.";

  public static final String INVALID_UUID_MESSAGE = "Invalid id provided.";
  public static final String ARGUMENT_TYPE_MISMATCH_MESSAGE =
      "The value you entered is not in the correct format.";

  // HTTP 500 - Internal Server Error
  public static final String DATABASE_ERROR_MESSAGE =
      "A database access error occurred. This may be due to connection issues, invalid SQL, or other database-related problems.";
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "An unexpected internal server error occurred. We are working to resolve this issue. Please try again later.";

  private ExceptionConstants() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
