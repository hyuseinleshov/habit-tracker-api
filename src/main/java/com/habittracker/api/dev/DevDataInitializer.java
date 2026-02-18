package com.habittracker.api.dev;

import com.habittracker.api.auth.dto.RegisterRequest;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.AuthService;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.service.UserProfileService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final AuthService authService;
  private final UserProfileService userProfileService;
  private final RefreshTokenService refreshTokenService;

  @Override
  @Transactional
  public void run(String... args) {
    log.info("Initializing data for dev profile");
    initRoles();
    initUsers();
  }

  private void initRoles() {
    if (roleRepository.count() == 0) {
      log.info("Initializing roles");

      Arrays.stream(RoleType.values())
          .forEach(
              roleType -> {
                RoleEntity role = new RoleEntity();
                role.setType(roleType);
                roleRepository.save(role);
                log.info("Created role: {}", roleType);
              });
    }
  }

  private void initUsers() {
    if (userRepository.count() == 0) {
      log.info("Initializing users");

      // Create regular user
      RegisterRequest userRequest = new RegisterRequest("user@example.com", "user123", "UTC");
      authService.register(userRequest);
      log.info("Created regular user: {}", userRequest.email());

      // Set profile info for regular user
      userRepository
          .findByEmail(userRequest.email())
          .ifPresent(
              user -> {
                if (user.getUserProfile() != null) {
                  UserProfileDTO userProfileDTO =
                      new UserProfileDTO(null, "Regular", "User", 25, "UTC");
                  userProfileService.update(user.getId(), userProfileDTO);
                }
                refreshTokenService.revokeAllRefreshTokensForUser(user.getId());
              });

      // Create admin user
      RegisterRequest adminRequest = new RegisterRequest("admin@example.com", "admin123", "UTC");
      authService.register(adminRequest);
      log.info("Created admin user: {}", adminRequest.email());

      // Set profile info and admin role for admin user
      userRepository
          .findByEmail(adminRequest.email())
          .ifPresent(
              user -> {
                if (user.getUserProfile() != null) {
                  UserProfileDTO userProfileDTO =
                      new UserProfileDTO(null, "Admin", "User", 30, "UTC");
                  userProfileService.update(user.getId(), userProfileDTO);
                }

                // Assign ADMIN role
                RoleEntity adminRole = roleRepository.getByType(RoleType.ADMIN);
                user.getRoles().add(adminRole);
                userRepository.save(user);
                log.info("Assigned ADMIN role to user: {}", user.getEmail());
                refreshTokenService.revokeAllRefreshTokensForUser(user.getId());
              });
    }
  }
}
