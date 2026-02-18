package com.habittracker.api.user.service.impl;

import static com.habittracker.api.user.constants.UserProfileConstants.EMAIL_CANT_BE_NULL_MESSAGE;
import static com.habittracker.api.user.constants.UserProfileConstants.USER_CANT_BE_NULL_MESSAGE;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.auth.repository.UserRepository;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.security.jwt.service.JwtBlacklistService;
import com.habittracker.api.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RefreshTokenService refreshTokenService;
  private final JwtBlacklistService blacklistService;

  @Override
  public void updateEmail(UserEntity user, String email) {
    checkForNullUser(user);
    if (email == null) throw new IllegalArgumentException(EMAIL_CANT_BE_NULL_MESSAGE);
    blacklistService.revokeActiveToken(user.getId());
    user.setEmail(email);
    userRepository.save(user);
  }

  @Override
  public void delete(UUID userId) {
    log.debug("Soft delete user with id {}", userId);
    UserEntity user = userRepository.getReferenceById(userId);
    Instant now = Instant.now();
    user.setDeletedAt(now);
    user.getHabits().forEach(h -> h.setDeletedAt(now));
    refreshTokenService.revokeAllRefreshTokensForUser(user.getId());
    blacklistService.revokeActiveToken(userId);
  }

  private void checkForNullUser(UserEntity user) {
    if (user == null) {
      throw new IllegalArgumentException(USER_CANT_BE_NULL_MESSAGE);
    }
  }
}
