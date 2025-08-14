package com.habittracker.api.userprofile.mapper;

import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.model.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    UserProfileDTO toUserProfileDTO(UserProfileEntity userProfileEntity);
}
