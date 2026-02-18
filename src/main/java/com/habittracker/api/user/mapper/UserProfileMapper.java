package com.habittracker.api.user.mapper;

import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.model.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  @Mapping(target = "email", source = "user.email")
  UserProfileDTO toUserProfileDTO(UserProfileEntity userProfileEntity);
}
