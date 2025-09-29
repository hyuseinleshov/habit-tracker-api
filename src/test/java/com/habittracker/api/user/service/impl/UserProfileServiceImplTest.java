package com.habittracker.api.user.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_PROFILE_DATA_NOT_VALID_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.utils.TimeZoneUtils;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.mapper.UserProfileMapper;
import com.habittracker.api.user.model.UserProfileEntity;
import com.habittracker.api.user.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

  private static final String TEST_TIMEZONE = "America/Santiago";
  private static final UserEntity TEST_USER =
      createUser("test@gmail.com", "pass", createUserRole());
  private static final String TEST_FIRST_NAME = "John";
  private static final String TEST_LAST_NAME = "Doe";
  private static final Integer TEST_AGE = 22;
  public static final UserProfileDTO TEST_USER_PROFILE_DTO =
      new UserProfileDTO(null, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE, TEST_TIMEZONE);
  private static final UserProfileEntity TEST_PROFILE =
      new UserProfileEntity(TEST_USER, TEST_TIMEZONE, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE);

  @Mock private UserProfileMapper profileMapper;
  @Mock private UserService userService;
  @Mock private Validator validator;

  @InjectMocks private UserProfileServiceImpl toTest;

  @Test
  void test_CreateProfile_Should_ThrowException_When_User_isNull() {
    assertThatThrownBy(() -> toTest.createProfile(null, TEST_TIMEZONE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_CANT_BE_NULL_MESSAGE);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Europe"})
  @NullSource
  void test_CreateProfile_Should_ThrowException_When_Timezone_Is_Invalid(String timezone) {
    assertThatThrownBy(() -> toTest.createProfile(TEST_USER, timezone))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(TimeZoneUtils.INVALID_TIMEZONE_MESSAGE);
  }

  @Test
  void test_CreateProfile_Should_Return_ExpectedResult_WithValid_Arguments() {
    UserProfileEntity savedProfile = toTest.createProfile(TEST_USER, TEST_TIMEZONE);
    assertThat(savedProfile.getTimezone()).isEqualTo(TEST_TIMEZONE);
    assertThat(TEST_USER).isEqualTo(savedProfile.getUser());
  }

  @Test
  void test_GetUserProfileById_Should_Return_ExpectedResult_With_Valid_Id() {
    when(profileMapper.toUserProfileDTO(TEST_PROFILE)).thenReturn(TEST_USER_PROFILE_DTO);

    UserProfileDTO profile = toTest.toProfileDTO(TEST_PROFILE);

    verify(profileMapper).toUserProfileDTO(TEST_PROFILE);

    assertThat(profile.firstName()).isEqualTo(TEST_FIRST_NAME);
    assertThat(profile.lastName()).isEqualTo(TEST_LAST_NAME);
    assertThat(profile.age()).isEqualTo(TEST_AGE);
    assertThat(profile.timezone()).isEqualTo(TEST_TIMEZONE);
  }

  @Test
  @SuppressWarnings("unchecked")
  void test_UpdateProfile_Should_Throw_WithInvalid_ProfileData() {
    HashSet<ConstraintViolation<Object>> mockSet = mock(HashSet.class);
    when(mockSet.isEmpty()).thenReturn(false);
    when(validator.validate(any())).thenReturn(mockSet);
    assertThatThrownBy(() -> toTest.update(TEST_PROFILE, TEST_USER_PROFILE_DTO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_PROFILE_DATA_NOT_VALID_MESSAGE);
  }

  @Test
  void test_UpdateProfile_Should_Update_UserProfile_With_Valid_Arguments() {
    final String UPDATED_FIRST_NAME = "George";
    final String UPDATED_EMAIL = "George";
    final String UPDATED_LAST_NAME = "Ivanov";
    final Integer UPDATED_AGE = 30;
    final String UPDATED_TIMEZONE = "Africa/Nairobi";

    UserProfileDTO updatedProfileDTO =
        new UserProfileDTO(
            UPDATED_EMAIL, UPDATED_FIRST_NAME, UPDATED_LAST_NAME, UPDATED_AGE, UPDATED_TIMEZONE);

    toTest.update(TEST_PROFILE, updatedProfileDTO);

    ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);

    verify(profileMapper).toUserProfileDTO(captor.capture());
    verify(userService).updateEmail(TEST_USER, UPDATED_EMAIL);
    UserProfileEntity updatedProfile = captor.getValue();

    assertThat(updatedProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
    assertThat(updatedProfile.getLastName()).isEqualTo(UPDATED_LAST_NAME);
    assertThat(updatedProfile.getAge()).isEqualTo(UPDATED_AGE);
    assertThat(updatedProfile.getTimezone()).isEqualTo(UPDATED_TIMEZONE);
  }
}
