package com.habittracker.api.user.controller;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.auth.service.RefreshTokenService;
import com.habittracker.api.auth.utils.RefreshTokenCookieUtils;
import com.habittracker.api.user.dto.UserProfileDTO;
import com.habittracker.api.user.service.UserProfileService;
import com.habittracker.api.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/me")
@RequiredArgsConstructor
public class UserController {

  private final UserProfileService userProfileService;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;
  private final RefreshTokenCookieUtils refreshTokenCookieUtils;

  @GetMapping
  public ResponseEntity<UserProfileDTO> get(@AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(userProfileService.toProfileDTO(principal.id()));
  }

  @PutMapping
  public ResponseEntity<UserProfileDTO> update(
      @RequestBody @Valid UserProfileDTO userProfileDTO,
      @AuthenticationPrincipal UserDetailsImpl principal,
      HttpServletResponse httpResponse) {
    UserProfileDTO response = userProfileService.update(principal.id(), userProfileDTO);
    refreshTokenService.revokeAllRefreshTokensForUser(principal.id());
    refreshTokenCookieUtils.addRefreshTokenCookie(
        refreshTokenService.createRefreshToken(principal.id()), httpResponse);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserDetailsImpl principal, HttpServletResponse httpResponse) {
    userService.delete(principal.id());
    refreshTokenCookieUtils.clearRefreshTokenCookie(httpResponse);
    return ResponseEntity.noContent().build();
  }
}
