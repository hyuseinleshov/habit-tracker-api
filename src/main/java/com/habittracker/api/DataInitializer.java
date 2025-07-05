package com.habittracker.api;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;

  @Override
  public void run(String... args) {
    Arrays.stream(RoleType.values())
        .forEach(
            roleType -> {
              if (roleRepository.findByType(roleType).isEmpty()) {
                log.info("Creating role: {}", roleType);
                RoleEntity role = new RoleEntity();
                role.setType(roleType);
                roleRepository.save(role);
              }
            });
  }
}
