package com.habittracker.api.user.mapper;

import com.habittracker.api.auth.model.UserEntity;
import com.habittracker.api.user.dto.AdminUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

  @Mapping(target = "timeZone", source = "userProfile.timeZone")
  @Mapping(target = "firstName", source = "userProfile.firstName")
  @Mapping(target = "lastName", source = "userProfile.lastName")
  @Mapping(target = "age", source = "userProfile.age")
  AdminUserDTO toAdminUserDTO(UserEntity user);
}
