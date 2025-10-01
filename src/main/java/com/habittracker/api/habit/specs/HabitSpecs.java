package com.habittracker.api.habit.specs;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.entity.DeletableEntity_;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.model.HabitEntity_;
import org.springframework.data.jpa.domain.Specification;

public class HabitSpecs {

  public static Specification<HabitEntity> isDeleted(boolean isDeleted) {
    return (root, query, builder) -> {
      if (isDeleted) {
        return builder.isNotNull(root.get(DeletableEntity_.deletedAt));
      }
      return builder.isNull(root.get(DeletableEntity_.deletedAt));
    };
  }

  public static Specification<HabitEntity> isArchived(boolean isArchived) {
    return (root, query, builder) -> builder.equal(root.get(HabitEntity_.archived), isArchived);
  }

  public static Specification<HabitEntity> hasUser(UserEntity user) {
    return (root, query, builder) -> builder.equal(root.get(HabitEntity_.user), user);
  }
}
