package com.habittracker.api;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import com.habittracker.api.auth.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
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
      RoleEntity userRole =
          roleRepository
              .getByType(RoleType.USER);

      UserEntity regularUser = new UserEntity();
      regularUser.setEmail("user@example.com");
      regularUser.setPassword(passwordEncoder.encode("user123"));
      regularUser.setRoles(Collections.singleton(userRole));
      userRepository.save(regularUser);
      log.info("Created regular user: {}", regularUser.getEmail());

      // Create admin user
      RoleEntity adminRole =
          roleRepository
              .getByType(RoleType.ADMIN);

      UserEntity adminUser = new UserEntity();
      adminUser.setEmail("admin@example.com");
      adminUser.setPassword(passwordEncoder.encode("admin123"));
      Set<RoleEntity> adminRoles = new HashSet<>();
      adminRoles.add(userRole);
      adminRoles.add(adminRole);
      adminUser.setRoles(adminRoles);
      userRepository.save(adminUser);
      log.info("Created admin user: {}", adminUser.getEmail());
    }
  }
}
