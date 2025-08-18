package com.habittracker.api.userprofile.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.createUser;
import static com.habittracker.api.auth.testutils.AuthTestUtils.createUserRole;
import static com.habittracker.api.userprofile.constants.UserProfileConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.exception.UserProfileNotFoundException;
import com.habittracker.api.userprofile.mapper.UserProfileMapper;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
  private static final UUID TEST_ID = UUID.randomUUID();
  private static final String TEST_FIRST_NAME = "John";
  private static final String TEST_LAST_NAME = "Doe";
  private static final Integer TEST_AGE = 22;
  public static final UserProfileDTO TEST_USER_PROFILE_DTO =
      new UserProfileDTO(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE, TEST_TIMEZONE);
  private static final UserProfileEntity TEST_PROFILE =
      new UserProfileEntity(TEST_USER, TEST_TIMEZONE, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_AGE);

  @Mock private UserProfileRepository userProfileRepository;
  @Mock private UserProfileMapper profileMapper;

  private static Validator validator;

  @InjectMocks private UserProfileServiceImpl toTest;

  @BeforeAll
  static void beforeAll() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @BeforeEach
  void setUp() {
    this.toTest = new UserProfileServiceImpl(userProfileRepository, profileMapper, validator);
  }

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
        .hasMessage(INVALID_TIMEZONE_MESSAGE);
  }

  @Test
  void test_CreateProfile_Should_Return_ExpectedResult_WithValid_Arguments() {
    toTest.createProfile(TEST_USER, TEST_TIMEZONE);
    ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
    verify(userProfileRepository).save(captor.capture());
    UserProfileEntity savedProfile = captor.getValue();
    assertThat(savedProfile.getTimezone()).isEqualTo(TEST_TIMEZONE);
    assertThat(TEST_USER).isEqualTo(savedProfile.getUser());
  }

  @Test
  void test_GetUserProfileById_Should_Throw_When_Id_Is_Invalid() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> toTest.getById(TEST_ID))
        .isInstanceOf(UserProfileNotFoundException.class)
        .hasMessage(USER_PROFILE_NOT_FOUND_MESSAGE);
  }

  @Test
  void test_GetUserProfileById_Should_Return_ExpectedResult_With_Valid_Id() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.of(TEST_PROFILE));
    when(profileMapper.toUserProfileDTO(TEST_PROFILE)).thenReturn(TEST_USER_PROFILE_DTO);

    UserProfileDTO profile = toTest.getById(TEST_ID);

    verify(userProfileRepository).findById(TEST_ID);
    verify(profileMapper).toUserProfileDTO(TEST_PROFILE);

    assertThat(profile.firstName()).isEqualTo(TEST_FIRST_NAME);
    assertThat(profile.lastName()).isEqualTo(TEST_LAST_NAME);
    assertThat(profile.age()).isEqualTo(TEST_AGE);
    assertThat(profile.timezone()).isEqualTo(TEST_TIMEZONE);
  }

  @ParameterizedTest
  @MethodSource(
      "com.habittracker.api.userprofile.testutils.UserProfileTestUtils#invalidUserProfileDTOs")
  void test_UpdateProfile_Should_Throw_WithInvalid_ProfileData(UserProfileDTO profile) {
    assertThatThrownBy(() -> toTest.update(TEST_ID, profile))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_PROFILE_DATA_NOT_VALID_MESSAGE);
  }

  @Test
  void test_UpdateProfile_Should_Throw_With_Invalid_Id() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> toTest.update(TEST_ID, TEST_USER_PROFILE_DTO))
        .isInstanceOf(UserProfileNotFoundException.class)
        .hasMessage(USER_PROFILE_NOT_FOUND_MESSAGE);
  }

  @Test
  void test_UpdateProfile_Should_Update_UserProfile_With_Valid_Arguments() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.of(TEST_PROFILE));
    final String UPDATED_FIRST_NAME = "George";
    final String UPDATED_LAST_NAME = "Ivanov";
    final Integer UPDATED_AGE = 30;
    final String UPDATED_TIMEZONE = "Africa/Nairobi";

    UserProfileDTO updatedProfileDTO =
        new UserProfileDTO(UPDATED_FIRST_NAME, UPDATED_LAST_NAME, UPDATED_AGE, UPDATED_TIMEZONE);

    toTest.update(TEST_ID, updatedProfileDTO);

    ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);

    verify(userProfileRepository).save(captor.capture());
    UserProfileEntity updatedProfile = captor.getValue();

    assertThat(updatedProfile.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
    assertThat(updatedProfile.getLastName()).isEqualTo(UPDATED_LAST_NAME);
    assertThat(updatedProfile.getAge()).isEqualTo(UPDATED_AGE);
    assertThat(updatedProfile.getTimezone()).isEqualTo(UPDATED_TIMEZONE);
  }

  @Test
  void test_DeleteUser_Should_Throw_When_Id_Is_Invalid() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> toTest.delete(TEST_ID))
            .isInstanceOf(UserProfileNotFoundException.class)
            .hasMessage(USER_PROFILE_NOT_FOUND_MESSAGE);
  }

  @Test
  void test_Delete_Should_Delete_UserProfile_WithValid_Id() {
    when(userProfileRepository.findById(TEST_ID)).thenReturn(Optional.of(TEST_PROFILE));

    Instant before = Instant.now();
    toTest.delete(TEST_ID);
    Instant after = Instant.now();

    assertThat(TEST_PROFILE.getUser().isDeleted()).isTrue();
    assertThat(TEST_PROFILE.getUser().getDeletedAt()).isInstanceOf(Instant.class)
                    .isBetween(before.minusSeconds(1), after.plusSeconds(1));

    verify(userProfileRepository).findById(TEST_ID);
  }
}
