package com.habittracker.api.habit.specs;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.habit.model.HabitEntity;
import org.springframework.data.jpa.domain.Specification;

public class HabitSpecs {

    public static Specification<HabitEntity> isDeleted(boolean isDeleted) {
        return (root, query, builder)
                ->  {
            if(isDeleted) {
                return builder.isNotNull(root.get("deletedAt"));
            }
            return builder.isNull(root.get("deletedAt"));
        };
    }

    public static Specification<HabitEntity> isArchived(boolean isArchived) {
        return (root, query, builder)
                -> builder.equal(root.get("archived") , isArchived);
    }

    public static Specification<HabitEntity> hasUser(UserEntity user) {
        return (root, query, builder)
                -> builder.equal(root.get("user"), user);
    }

}
