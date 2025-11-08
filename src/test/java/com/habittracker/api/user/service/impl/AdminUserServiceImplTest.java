package com.habittracker.api.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.user.dto.AdminUserDTO;
import com.habittracker.api.user.mapper.AdminUserMapper;
import com.habittracker.api.user.model.UserProfileEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AdminUserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private AdminUserMapper adminUserMapper;
  @InjectMocks private AdminUserServiceImpl adminUserService;

  @Test
  void getAllUsers_WithIncludeDeletedTrue_ShouldReturnAllUsers() {
    UserEntity user1 = createUser("user1@example.com", null);
    UserEntity user2 = createUser("user2@example.com", Instant.now());

    AdminUserDTO dto1 = createAdminUserDTO("user1@example.com", null);
    AdminUserDTO dto2 = createAdminUserDTO("user2@example.com", Instant.now());

    Page<UserEntity> userPage = new PageImpl<>(List.of(user1, user2));
    Pageable pageable = PageRequest.of(0, 20);

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
    when(adminUserMapper.toAdminUserDTO(user1)).thenReturn(dto1);
    when(adminUserMapper.toAdminUserDTO(user2)).thenReturn(dto2);

    PagedModel<AdminUserDTO> result = adminUserService.getAllUsers(pageable, true);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).containsExactly(dto1, dto2);
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getAllUsers_WithIncludeDeletedFalse_ShouldReturnOnlyActiveUsers() {
    UserEntity activeUser = createUser("active@example.com", null);
    AdminUserDTO dto = createAdminUserDTO("active@example.com", null);

    Page<UserEntity> userPage = new PageImpl<>(List.of(activeUser));
    Pageable pageable = PageRequest.of(0, 20);

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
    when(adminUserMapper.toAdminUserDTO(activeUser)).thenReturn(dto);

    PagedModel<AdminUserDTO> result = adminUserService.getAllUsers(pageable, false);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().deletedAt()).isNull();
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getAllUsers_WithEmptyResult_ShouldReturnEmptyPage() {
    Page<UserEntity> emptyPage = new PageImpl<>(List.of());
    Pageable pageable = PageRequest.of(0, 20);

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

    PagedModel<AdminUserDTO> result = adminUserService.getAllUsers(pageable, false);

    assertThat(result.getContent()).isEmpty();
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  private UserEntity createUser(String email, Instant deletedAt) {
    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setDeletedAt(deletedAt);

    UserProfileEntity profile = new UserProfileEntity();
    profile.setTimeZone("UTC");
    profile.setFirstName("Test");
    profile.setLastName("User");
    profile.setAge(25);
    user.setUserProfile(profile);

    return user;
  }

  private AdminUserDTO createAdminUserDTO(String email, Instant deletedAt) {
    return new AdminUserDTO(email, "UTC", "Test", "User", 25, deletedAt);
  }
}
