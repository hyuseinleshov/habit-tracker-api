package com.habittracker.api.habit.model;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.core.entity.DeletableEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "habits")
@Getter
@Setter
public class HabitEntity extends DeletableEntity {

  @ManyToOne
  @JoinColumn(nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private boolean archived;

  @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL)
  private Set<CheckInEntity> checkIns = new HashSet<>();
}
