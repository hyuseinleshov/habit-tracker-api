package com.habittracker.api.userprofile.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.userprofile.dto.UserProfileDTO;
import com.habittracker.api.userprofile.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/me")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;

  @GetMapping
  public ResponseEntity<UserProfileDTO> get(@AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(userProfileService.getById(principal.getId()));
  }

  @PutMapping
  public ResponseEntity<UserProfileDTO> update(
      @RequestBody @Valid UserProfileDTO userProfileDTO,
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(userProfileService.update(principal.getId(), userProfileDTO));
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetailsImpl principal) {
    userProfileService.delete(principal.getId());
    return ResponseEntity.noContent().build();
  }
}
