package com.habittracker.api.habit.model;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "habits")
@Getter
@Setter
public class HabitEntity extends BaseEntity {

  @ManyToOne
  @JoinColumn(nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String name;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Frequency frequency;

  @Column(nullable = false)
  private boolean archived;
}
