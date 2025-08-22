package com.habittracker.api.user.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final UserEntity TEST_USER =
      createUser("test@gmail.com", "pass", createUserRole());

  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private UserServiceImpl toTest;

  @Test
  void test_DeleteUser_Should_Throw_When_Id_Is_Invalid() {

    assertThatThrownBy(() -> toTest.delete(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_CANT_BE_NULL_MESSAGE);
  }

  @Test
  void test_Delete_Should_Delete_UserProfile_WithValid_Id() {

    Instant before = Instant.now();
    toTest.delete(TEST_USER);
    Instant after = Instant.now();

    assertThat(TEST_USER.isDeleted()).isTrue();
    assertThat(TEST_USER.getDeletedAt())
        .isInstanceOf(Instant.class)
        .isBetween(before.minusSeconds(1), after.plusSeconds(1));
    verify(refreshTokenService).revokeAllRefreshTokensForUser(TEST_USER.getEmail());
  }
}
