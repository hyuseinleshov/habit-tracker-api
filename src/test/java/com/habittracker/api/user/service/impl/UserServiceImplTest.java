package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_NOT_FOUND_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.user.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final UUID TEST_ID = UUID.randomUUID();
  private static final UserEntity TEST_USER =
      createUser("test@gmail.com", "pass", createUserRole());

  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private UserServiceImpl toTest;

  @Test
  void test_DeleteUser_Should_Throw_When_Id_Is_Invalid() {
    when(userRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> toTest.delete(TEST_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage(USER_NOT_FOUND_MESSAGE);
  }

  @Test
  void test_Delete_Should_Delete_UserProfile_WithValid_Id() {
    when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(TEST_USER));

    Instant before = Instant.now();
    toTest.delete(TEST_ID);
    Instant after = Instant.now();

    assertThat(TEST_USER.isDeleted()).isTrue();
    assertThat(TEST_USER.getDeletedAt())
        .isInstanceOf(Instant.class)
        .isBetween(before.minusSeconds(1), after.plusSeconds(1));
    verify(refreshTokenService).revokeAllRefreshTokensForUser(TEST_USER.getEmail());
    verify(userRepository).findById(TEST_ID);
  }
}
