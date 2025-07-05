package com.habittracker.api.auth.mapper;

import com.habittracker.api.auth.dto.UserDto;
import com.habittracker.api.auth.model.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(UserEntity user);
}
