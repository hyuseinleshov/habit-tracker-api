package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.user.constants.UserProfileConstants.EMAIL_CANT_BE_NULL_MESSAGE;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.security.jwt.service.JwtBlacklistService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final UserEntity TEST_USER =
      createUser("test@gmail.com", "pass", createUserRole());

  @Mock private RefreshTokenService refreshTokenService;
  @Mock private UserRepository userRepository;
  @Mock private JwtBlacklistService jwtBlacklistService;

  @InjectMocks private UserServiceImpl toTest;

  @Test
  void test_Delete_Should_Delete_UserProfile_WithValid_Id() {
    when(userRepository.getReferenceById(TEST_USER.getId())).thenReturn(TEST_USER);
    Instant before = Instant.now();
    toTest.delete(TEST_USER.getId());
    Instant after = Instant.now();

    assertThat(TEST_USER.isDeleted()).isTrue();
    assertThat(TEST_USER.getDeletedAt())
        .isInstanceOf(Instant.class)
        .isBetween(before.minusSeconds(1), after.plusSeconds(1));
    verify(refreshTokenService).revokeAllRefreshTokensForUser(TEST_USER.getId());
  }

  @Test
  void test_updateEmail_Should_Throw_With_Null_User() {
    assertThatThrownBy(() -> toTest.updateEmail(null, "test-mail@gmail.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_CANT_BE_NULL_MESSAGE);
  }

  @Test
  void test_updateEmail_Should_Throw_With_Null_Email() {
    assertThatThrownBy(() -> toTest.updateEmail(TEST_USER, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(EMAIL_CANT_BE_NULL_MESSAGE);
  }

  @Test
  void test_updateEmail_Should_UpdateEmail_With_ValidUser() {
    final String NEW_EMAIL = "new@gmail.com";
    toTest.updateEmail(TEST_USER, NEW_EMAIL);
    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getEmail()).isEqualTo(NEW_EMAIL);
  }
}
