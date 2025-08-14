package com.habittracker.api.userprofile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileDTO(String firstName, String lastName, Integer age, String timezone) {
}
