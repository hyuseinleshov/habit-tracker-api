package com.habittracker.api.auth.testutils;

import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleTestUtils {

  private final RoleRepository roleRepository;

  public RoleTestUtils(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public void setUpRoles() {
    if (roleRepository.count() != 0) return;
    RoleEntity userRole = new RoleEntity();
    userRole.setType(RoleType.USER);

    RoleEntity adminRole = new RoleEntity();
    adminRole.setType(RoleType.ADMIN);

    roleRepository.save(userRole);
    roleRepository.save(adminRole);
  }
}
