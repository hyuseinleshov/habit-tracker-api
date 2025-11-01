package com.habittracker.api.checkin.specs;

import com.habittracker.api.auth.model.UserEntity_;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.model.CheckInEntity_;
import com.habittracker.api.core.entity.BaseEntity_;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.model.HabitEntity_;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class CheckInSpecs {

  public static Specification<CheckInEntity> hasHabit(HabitEntity habit) {
    return (root, query, builder) -> builder.equal(root.get(CheckInEntity_.habit), habit);
  }

  public static Specification<CheckInEntity> hasUser(UUID userId) {
    return (root, query, builder) -> {
      Join<CheckInEntity, HabitEntity> habitJoin = root.join(CheckInEntity_.habit);
      return builder.equal(habitJoin.get(HabitEntity_.user).get(UserEntity_.id), userId);
    };
  }

  public static Specification<CheckInEntity> createdAfter(Instant from) {
    return (root, query, builder) -> {
      if (from == null) {
        return builder.conjunction();
      }
      return builder.greaterThanOrEqualTo(root.get(BaseEntity_.createdAt), from);
    };
  }

  public static Specification<CheckInEntity> createdBefore(Instant to) {
    return (root, query, builder) -> {
      if (to == null) {
        return builder.conjunction();
      }
      return builder.lessThanOrEqualTo(root.get(BaseEntity_.createdAt), to);
    };
  }
}
