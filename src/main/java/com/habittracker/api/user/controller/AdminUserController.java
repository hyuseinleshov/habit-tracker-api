package com.habittracker.api.user.controller;

import com.habittracker.api.user.dto.AdminUserDTO;
import com.habittracker.api.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  /**
   * Retrieves a paginated list of users in the system.
   *
   * <p>This endpoint is restricted to administrators and returns user accounts based on the
   * includeDeleted parameter. By default, only active users are returned. Each user record includes
   * essential information such as email, timeZone, profile details, and deletion status.
   *
   * @param pageable pagination parameters (page number, size, and sorting)
   * @param includeDeleted whether to include soft-deleted users in the results (default: false)
   * @return paginated response containing user information
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedModel<AdminUserDTO>> getAllUsers(
      @PageableDefault(sort = "createdAt") Pageable pageable,
      @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
    PagedModel<AdminUserDTO> users = adminUserService.getAllUsers(pageable, includeDeleted);
    return ResponseEntity.ok(users);
  }
}
