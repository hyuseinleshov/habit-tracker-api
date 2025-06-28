package com.habittracker.api.checkin;

import com.habittracker.api.core.entity.BaseEntity;
import com.habittracker.api.habit.HabitEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
public class CheckInEntity extends BaseEntity {

  @ManyToOne
  @JoinColumn(nullable = false)
  private HabitEntity habit;
}
