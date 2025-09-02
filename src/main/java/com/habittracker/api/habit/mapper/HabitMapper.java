package com.habittracker.api.habit.mapper;

import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.model.HabitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HabitMapper {

  @Mapping(
      target = "frequency",
      expression = "java(com.habittracker.api.habit.model.Frequency.DAILY)")
  HabitResponse toResponse(HabitEntity entity);
}
