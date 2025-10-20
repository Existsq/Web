package com.bmstu.lab.infrastructure.security;

import com.bmstu.lab.infrastructure.persistence.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    "Пользователь с никнеймом " + username + " не найден"));
  }
}
