package com.habittracker.api.auth.model;

import com.habittracker.api.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;
}
