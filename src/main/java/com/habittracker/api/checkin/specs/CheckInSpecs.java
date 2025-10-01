package com.habittracker.api.checkin.specs;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.checkin.model.CheckInEntity_;
import com.habittracker.api.core.entity.BaseEntity_;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.model.HabitEntity_;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public class CheckInSpecs {

  public static Specification<CheckInEntity> hasHabit(HabitEntity habit) {
    return (root, query, builder) -> builder.equal(root.get(CheckInEntity_.habit), habit);
  }

  public static Specification<CheckInEntity> hasUser(UserEntity user) {
    return (root, query, builder) -> {
      Join<CheckInEntity, HabitEntity> habitJoin = root.join(CheckInEntity_.habit);
      return builder.equal(habitJoin.get(HabitEntity_.user), user);
    };
  }

  public static Specification<CheckInEntity> createdAfter(Instant from) {
    return (root, query, builder) ->
        builder.greaterThanOrEqualTo(root.get(BaseEntity_.createdAt), from);
  }

  public static Specification<CheckInEntity> createdBefore(Instant to) {
    return (root, query, builder) -> builder.lessThanOrEqualTo(root.get(BaseEntity_.createdAt), to);
  }
}
