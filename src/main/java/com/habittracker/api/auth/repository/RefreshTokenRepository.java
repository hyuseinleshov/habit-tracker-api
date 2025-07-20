package com.habittracker.api.auth.repository;

import com.habittracker.api.auth.model.RefreshTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByToken(String token);

  void deleteByToken(String token);

  void deleteAllByEmail(String email);
}
