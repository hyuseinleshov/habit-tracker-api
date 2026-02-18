package com.habittracker.api.user.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.user.dto.AdminUserDTO;
import com.habittracker.api.user.mapper.AdminUserMapper;
import com.habittracker.api.user.service.AdminUserService;
import com.habittracker.api.user.specs.UserSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;
  private final AdminUserMapper adminUserMapper;

  @Override
  @Transactional(readOnly = true)
  public PagedModel<AdminUserDTO> getAllUsers(Pageable pageable, boolean includeDeleted) {
    Page<UserEntity> users =
        userRepository.findAll(UserSpecs.includeDeleted(includeDeleted), pageable);
    return new PagedModel<>(users.map(adminUserMapper::toAdminUserDTO));
  }
}
