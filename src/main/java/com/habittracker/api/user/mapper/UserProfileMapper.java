package com.habittracker.api.user.mapper;

import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.model.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

  UserProfileDTO toUserProfileDTO(UserProfileEntity userProfileEntity);
}
