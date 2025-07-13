package com.habittracker.api.auth.utils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.api.auth.dto.AuthRequest;
import com.habittracker.api.auth.model.RoleEntity;
import com.habittracker.api.auth.model.RoleType;
import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.RoleRepository;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public final class AuthTestUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

  public static RoleEntity getUserRoleFromRepository(RoleRepository roleRepository) {
    RoleEntity userRole = new RoleEntity();
    userRole.setType(RoleType.USER);
    roleRepository.save(userRole);
    return roleRepository.getByType(RoleType.USER);
  }

  public static ResultActions performPostRequest(
      MockMvc mockMvc, String endpoint, AuthRequest request) throws Exception {
    return mockMvc.perform(
        post(endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(request)));
  }

  private AuthTestUtils() {
    throw new UnsupportedOperationException("Utility class, do not instantiate");
  }
}
