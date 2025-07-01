package com.habittracker.api.auth.service;

import com.habittracker.api.auth.model.UserDetailsImpl;
import com.habittracker.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Authenticating user with email: {}", email);
    return userRepository
        .findByEmail(email)
        .map(UserDetailsImpl::new)
        .orElseThrow(
            () -> {
              log.warn("Authentication failed: email {} not found", email);
              return new UsernameNotFoundException(
                  String.format("User with email %s not found", email));
            });
  }
}
