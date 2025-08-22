package com.habittracker.api.user.testutils;

import static com.habittracker.api.config.constants.AuthTestConstants.TEST_TIMEZONE;

import com.habittracker.api.user.dto.UserProfileDTO;
import java.util.stream.Stream;

public final class UserProfileTestUtils {

  private UserProfileTestUtils() {}

  private static Stream<UserProfileDTO> invalidUserProfileDTOs() {
    return Stream.of(
        new UserProfileDTO("ethe", "Shenol", "test", 2, "Wrong"),
        new UserProfileDTO("messi", "Shenol", "test", -61, TEST_TIMEZONE),
        new UserProfileDTO(
            "test@gmail.com",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxy",
            "Shengov",
            12,
            TEST_TIMEZONE),
        new UserProfileDTO(
            "example@gmail.com",
            "Shenol",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxy",
            24,
            TEST_TIMEZONE),
        new UserProfileDTO(
            "eeee",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxy",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxy",
            -20,
            "Test"));
  }
}
