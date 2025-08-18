package com.habittracker.api.auth.testutils;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import com.habittracker.api.userprofile.repository.UserProfileRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthTestUtils {

  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;

  public AuthTestUtils(
      PasswordEncoder passwordEncoder,
      RoleRepository roleRepository,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository) {
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
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
    userProfileRepository.save(userProfileEntity);
    return user;
  }

  @Transactional
  public void softDelete(UserEntity userEntity) {
    userEntity.setDeletedAt(Instant.now());
  }

  public static UserEntity createUser(String email, String password, RoleEntity role) {
    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setPassword(password);
    user.setRoles(Set.of(role));
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
