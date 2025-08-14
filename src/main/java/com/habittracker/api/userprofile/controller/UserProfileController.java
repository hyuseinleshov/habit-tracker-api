package com.habittracker.api.userprofile.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/me")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;

  @GetMapping
  public ResponseEntity<UserProfileDTO> get(@AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(userProfileService.getUserProfileById(principal.getId()));
  }
}
