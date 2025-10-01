package com.habittracker.api.checkin.mapper;

import com.habittracker.api.checkin.CheckInResponse;
import com.habittracker.api.checkin.dto.CheckInWithHabitResponse;
import com.habittracker.api.checkin.model.CheckInEntity;
import com.habittracker.api.habit.mapper.HabitMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {HabitMapper.class})
public interface CheckInMapper {

  @Mapping(target = "habitId", source = "habit.id")
  @Mapping(target = "createdAt", expression = "java(Instant.now())")
  CheckInResponse toResponse(CheckInEntity entity);

  CheckInWithHabitResponse toResponseWithHabit(CheckInEntity entity);
}
