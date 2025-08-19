package com.habittracker.api.auth.repository;

import com.habittracker.api.auth.model.UserEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

  long deleteAllByDeletedAtBefore(Instant deletedAtBefore);
}
