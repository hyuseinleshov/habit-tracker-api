package com.habittracker.api.auth.model;

import com.habittracker.api.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RoleEntity extends BaseEntity {

  @EqualsAndHashCode.Include
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private RoleType type;
}
