package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.auth.utils.AuthConstants.EMAIL_EXISTS_MESSAGE;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.core.utils.TimeZoneUtils;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.mapper.UserProfileMapper;
import com.habittracker.api.user.model.UserProfileEntity;
import com.habittracker.api.user.repository.UserProfileRepository;
import com.habittracker.api.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

  private static final String TEST_TIME_ZONE = "America/Santiago";
  private static final UserEntity TEST_USER =
      createUser("test@gmail.com", "pass", createUserRole());
  private static final String TEST_FIRST_NAME = "John";
  private static final String TEST_LAST_NAME = "Doe";
  private static final Integer TEST_AGE = 22;
  public static final UserProfileDTO TEST_USER_PROFILE_DTO =
      new UserProfileDTO(null, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE, TEST_TIME_ZONE);
  private static final UserProfileEntity TEST_PROFILE =
      new UserProfileEntity(TEST_USER, TEST_TIME_ZONE, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE);

  @Mock private UserProfileMapper profileMapper;
  @Mock private UserProfileRepository userProfileRepository;
  @Mock private UserService userService;
  @Mock private UserRepository userRepository;

  @InjectMocks private UserProfileServiceImpl toTest;

  @Test
  void test_CreateProfile_Should_ThrowException_When_User_isNull() {
    assertThatThrownBy(() -> toTest.createProfile(null, TEST_TIME_ZONE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_CANT_BE_NULL_MESSAGE);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Europe"})
  @NullSource
  void test_CreateProfile_Should_ThrowException_When_Timezone_Is_Invalid(String timezone) {
    assertThatThrownBy(() -> toTest.createProfile(TEST_USER, timezone))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(TimeZoneUtils.INVALID_TIME_ZONE_MESSAGE);
  }

  @Test
  void test_CreateProfile_Should_Return_ExpectedResult_WithValid_Arguments() {
    UserProfileEntity savedProfile = toTest.createProfile(TEST_USER, TEST_TIME_ZONE);
    assertThat(savedProfile.getTimeZone()).isEqualTo(TEST_TIME_ZONE);
    assertThat(TEST_USER).isEqualTo(savedProfile.getUser());
  }

  @Test
  void test_GetUserProfileById_Should_Return_ExpectedResult_With_Valid_Id() {
    when(userProfileRepository.getReferenceById(TEST_PROFILE.getId())).thenReturn(TEST_PROFILE);
    when(profileMapper.toUserProfileDTO(TEST_PROFILE)).thenReturn(TEST_USER_PROFILE_DTO);

    UserProfileDTO profile = toTest.toProfileDTO(TEST_PROFILE.getId());

    verify(profileMapper).toUserProfileDTO(TEST_PROFILE);

    assertThat(profile.firstName()).isEqualTo(TEST_FIRST_NAME);
    assertThat(profile.lastName()).isEqualTo(TEST_LAST_NAME);
    assertThat(profile.age()).isEqualTo(TEST_AGE);
    assertThat(profile.timeZone()).isEqualTo(TEST_TIME_ZONE);
  }

  @Test
  void test_UpdateProfile_Should_Throw_When_Email_Already_Taken() {
    String takenEmail = "taken@gmail.com";
    UserProfileDTO dto = new UserProfileDTO(takenEmail, null, null, null, null);
    UserEntity existingUser = createUser(takenEmail, "pass", createUserRole());

    when(userProfileRepository.getReferenceById(TEST_PROFILE.getId())).thenReturn(TEST_PROFILE);
    when(userRepository.findByEmailAndDeletedAtIsNull(takenEmail))
        .thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> toTest.update(TEST_PROFILE.getId(), dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(EMAIL_EXISTS_MESSAGE);
  }

  @Test
  void test_UpdateProfile_Should_Update_Email_When_Changed() {
    String newEmail = "new@gmail.com";
    UserProfileDTO dto = new UserProfileDTO(newEmail, null, null, null, null);

    when(userProfileRepository.getReferenceById(TEST_PROFILE.getId())).thenReturn(TEST_PROFILE);
    when(userRepository.findByEmailAndDeletedAtIsNull(newEmail)).thenReturn(Optional.empty());

    toTest.update(TEST_PROFILE.getId(), dto);

    verify(userService).updateEmail(TEST_USER, newEmail);
    verify(profileMapper).updateProfileFromDto(dto, TEST_PROFILE);
    verify(profileMapper).toUserProfileDTO(TEST_PROFILE);
  }

  @Test
  void test_UpdateProfile_Should_Not_Update_Email_When_Same() {
    UserProfileDTO dto = new UserProfileDTO(TEST_USER.getEmail(), "NewFirst", null, null, null);

    when(userProfileRepository.getReferenceById(TEST_PROFILE.getId())).thenReturn(TEST_PROFILE);

    toTest.update(TEST_PROFILE.getId(), dto);

    verify(userService, never()).updateEmail(any(), any());
    verify(profileMapper).updateProfileFromDto(dto, TEST_PROFILE);
  }

  @Test
  void test_UpdateProfile_Should_Not_Update_Email_When_Null() {
    UserProfileDTO dto = new UserProfileDTO(null, "NewFirst", null, null, null);

    when(userProfileRepository.getReferenceById(TEST_PROFILE.getId())).thenReturn(TEST_PROFILE);

    toTest.update(TEST_PROFILE.getId(), dto);

    verify(userService, never()).updateEmail(any(), any());
    verify(profileMapper).updateProfileFromDto(dto, TEST_PROFILE);
  }
}
