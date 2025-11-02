package com.habittracker.api.user.specs;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.entity.DeletableEntity_;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecs {

  private UserSpecs() {}

  public static Specification<UserEntity> includeDeleted(boolean includeDeleted) {
    return (root, query, builder) -> {
      if (includeDeleted) {
        return builder.conjunction();
      }
      return builder.isNull(root.get(DeletableEntity_.deletedAt));
    };
  }
}
