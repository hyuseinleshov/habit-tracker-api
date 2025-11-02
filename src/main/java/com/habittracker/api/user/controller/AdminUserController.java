package com.habittracker.api.user.controller;

import com.habittracker.api.user.dto.AdminUserDTO;
import com.habittracker.api.user.service.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  /**
   * Retrieves a paginated list of all users in the system.
   *
   * <p>This endpoint is restricted to administrators and returns both active and soft-deleted user
   * accounts. Each user record includes essential information such as email, timezone, profile
   * details, and deletion status.
   *
   * @param pageable pagination parameters (page number, size, and sorting)
   * @return paginated response containing user information
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedModel<AdminUserDTO>> getAllUsers(
      @PageableDefault(sort = "createdAt") Pageable pageable) {
    PagedModel<AdminUserDTO> users = adminUserService.getAllUsers(pageable);
    return ResponseEntity.ok(users);
  }
}
