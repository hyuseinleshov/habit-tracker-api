package com.habittracker.api.auth.testutils;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.user.model.UserProfileEntity;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthTestUtils {

  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;

  public AuthTestUtils(
      PasswordEncoder passwordEncoder,
      RoleRepository roleRepository,
      UserRepository userRepository) {
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
  }

  public RoleEntity getUserRoleFromRepository() {
    if (roleRepository.getByType(RoleType.USER) == null) {
      RoleEntity userRole = new RoleEntity();
      userRole.setType(RoleType.USER);
      roleRepository.save(userRole);
    }
    return roleRepository.getByType(RoleType.USER);
  }

  public UserEntity createAndSaveUser(String email, String password, String timezone) {
    RoleEntity role = getUserRoleFromRepository();
    UserEntity user = createUser(email, passwordEncoder.encode(password), role);
    UserProfileEntity userProfileEntity = new UserProfileEntity();
    userProfileEntity.setTimezone(timezone);
    userProfileEntity.setUser(user);
    user.setUserProfile(userProfileEntity);
    return userRepository.save(user);
  }

  @Transactional
  public void softDelete(UserEntity userEntity, Instant deletedAt) {
    userEntity.setDeletedAt(deletedAt);
  }

  public static UserEntity createUser(String email, String password, RoleEntity role) {
    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setPassword(password);
    user.getRoles().add(role);
    return user;
  }

  public static RoleEntity createUserRole() {
    RoleEntity role = new RoleEntity();
    role.setType(RoleType.USER);
    return role;
  }

  public static String generateUserNotFoundMessage(String email) {
    return String.format("User with email %s not found", email);
  }
}
