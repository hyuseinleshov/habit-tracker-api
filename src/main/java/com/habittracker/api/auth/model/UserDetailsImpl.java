package com.habittracker.api.auth.model;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserDetailsImpl(UUID id, String email, boolean isAdmin, ZoneId timeZone)
    implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    HashSet<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + RoleType.USER));
    if (isAdmin) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + RoleType.ADMIN));
    }
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
