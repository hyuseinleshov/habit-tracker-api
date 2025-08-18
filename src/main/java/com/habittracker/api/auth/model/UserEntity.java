package com.habittracker.api.auth.model;

import com.habittracker.api.core.entity.DeletableEntity;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends DeletableEntity {

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleEntity> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private UserProfileEntity userProfile;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private Set<HabitEntity> habits = new HashSet<>();
}
