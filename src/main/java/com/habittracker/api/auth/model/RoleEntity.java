package com.habittracker.api.auth.model;

import com.habittracker.api.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class RoleEntity extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private RoleType type;
}
