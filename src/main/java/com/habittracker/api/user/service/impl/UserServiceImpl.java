package com.habittracker.api.user.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.user.exception.UserNotFoundException;
import com.habittracker.api.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RefreshTokenService refreshTokenService;

  @Override
  @Transactional
  public void delete(UUID id) {
    UserEntity toDelete = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    Instant now = Instant.now();
    toDelete.setDeletedAt(now);
    toDelete.getHabits().forEach(h -> h.setDeletedAt(now));
    refreshTokenService.revokeAllRefreshTokensForUser(toDelete.getEmail());
  }
}
