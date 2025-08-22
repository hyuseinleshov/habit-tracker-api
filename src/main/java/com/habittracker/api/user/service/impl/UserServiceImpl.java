package com.habittracker.api.user.service.impl;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RefreshTokenService refreshTokenService;

  @Override
  public void updateEmail(UserEntity user, String email) {
    checkForNullUser(user);
    refreshTokenService.revokeAllRefreshTokensForUser(user.getEmail());
    user.setEmail(email);
    userRepository.save(user);
  }

  @Override
  public void delete(UserEntity user) {
    checkForNullUser(user);
    String deletedEmail = user.getEmail();
    log.debug("Soft delete user with email {}", deletedEmail);
    Instant now = Instant.now();
    user.setDeletedAt(now);
    user.getHabits().forEach(h -> h.setDeletedAt(now));
    refreshTokenService.revokeAllRefreshTokensForUser(deletedEmail);
  }

  private void checkForNullUser(UserEntity user) {
    if (user == null) {
      throw new IllegalArgumentException(USER_CANT_BE_NULL_MESSAGE);
    }
  }
}
