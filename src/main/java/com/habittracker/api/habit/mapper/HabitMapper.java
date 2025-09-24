package com.habittracker.api.habit.mapper;

import com.habittracker.api.habit.dto.HabitResponse;
import com.habittracker.api.habit.dto.UpdateHabitRequest;
import com.habittracker.api.habit.model.HabitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HabitMapper {

  @Mapping(
      target = "frequency",
      expression = "java(com.habittracker.api.habit.model.Frequency.DAILY)")
  HabitResponse toResponse(HabitEntity entity);

  HabitEntity updateHabitFromUpdateRequest(UpdateHabitRequest updateHabitRequest, @MappingTarget HabitEntity habit);
}
