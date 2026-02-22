package com.habittracker.api.user.mapper;

import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.model.UserProfileEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  @Mapping(target = "email", source = "user.email")
  UserProfileDTO toUserProfileDTO(UserProfileEntity userProfileEntity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  void updateProfileFromDto(UserProfileDTO dto, @MappingTarget UserProfileEntity entity);
}
