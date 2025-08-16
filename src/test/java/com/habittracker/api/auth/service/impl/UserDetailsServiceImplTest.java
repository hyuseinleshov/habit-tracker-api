package com.habittracker.api.auth.service.impl;

import static com.habittracker.api.auth.testutils.AuthTestUtils.*;
import static com.habittracker.api.config.constants.AuthTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  private UserEntity testUser;
  private RoleEntity userRole;

  @BeforeEach
  void setUp() {
    userRole = createUserRole();
    testUser = createUser(TEST_EMAIL, ENCODED_PASSWORD, userRole);
  }

  @Test
  void givenExistingUser_whenLoadUserByUsername_thenReturnUserDetailsImpl() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

    assertThat(result).isInstanceOf(UserDetailsImpl.class);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;
    assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
    assertThat(userDetails.getPassword()).isEqualTo(ENCODED_PASSWORD);
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo(ROLE_USER_AUTHORITY);

    verify(userRepository).findByEmail(TEST_EMAIL);
  }

  @Test
  void
      givenUserWithMultipleRoles_whenLoadUserByUsername_thenReturnUserDetailsImplWithCorrectAuthorities() {
    RoleEntity adminRole = new RoleEntity();
    adminRole.setType(RoleType.ADMIN);
    testUser.setRoles(Set.of(userRole, adminRole));
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

    assertThat(result).isInstanceOf(UserDetailsImpl.class);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;
    assertThat(userDetails.getAuthorities()).hasSize(2);
    assertThat(userDetails.getAuthorities().stream().map(Object::toString))
        .containsExactlyInAnyOrder(ROLE_USER_AUTHORITY, ROLE_ADMIN_AUTHORITY);

    verify(userRepository).findByEmail(TEST_EMAIL);
  }

  @Test
  void givenNonExistentUser_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
    when(userRepository.findByEmail(NONEXISTENT_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(NONEXISTENT_EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage(generateUserNotFoundMessage(NONEXISTENT_EMAIL));

    verify(userRepository).findByEmail(NONEXISTENT_EMAIL);
  }

  @Test
  void
      givenEmptyEmail_whenLoadUserByUsername_thenThrowUsernameNotFoundExceptionWithCorrectMessage() {
    when(userRepository.findByEmail(EMPTY_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(EMPTY_EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage(generateUserNotFoundMessage(EMPTY_EMAIL));

    verify(userRepository).findByEmail(EMPTY_EMAIL);
  }

  @Test
  void
      givenNullEmail_whenLoadUserByUsername_thenThrowUsernameNotFoundExceptionWithCorrectMessage() {
    when(userRepository.findByEmail(NULL_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(NULL_EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage(generateUserNotFoundMessage(NULL_EMAIL));

    verify(userRepository).findByEmail(NULL_EMAIL);
  }

  @Test
  void givenUserWithNoRoles_whenLoadUserByUsername_thenReturnUserDetailsImplWithEmptyAuthorities() {
    testUser.setRoles(Set.of());
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

    assertThat(result).isInstanceOf(UserDetailsImpl.class);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;
    assertThat(userDetails.getAuthorities()).isEmpty();

    verify(userRepository).findByEmail(TEST_EMAIL);
  }

  @Test
  void
      givenExistingUser_whenLoadUserByUsername_thenReturnUserDetailsImplWithCorrectDefaultMethods() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

    assertThat(result).isInstanceOf(UserDetailsImpl.class);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;

    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    assertThat(userDetails.isEnabled()).isTrue();

    verify(userRepository).findByEmail(TEST_EMAIL);
  }
}
