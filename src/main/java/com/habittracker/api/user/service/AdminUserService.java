package com.habittracker.api.user.service;

import com.habittracker.api.user.dto.AdminUserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface AdminUserService {

  PagedModel<AdminUserDTO> getAllUsers(Pageable pageable);
}
