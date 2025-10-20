package com.bmstu.lab.application.service;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.application.exception.DuplicateUsernameException;
import com.bmstu.lab.application.exception.UserNotFoundException;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.infrastructure.persistence.mapper.UserMapper;
import com.bmstu.lab.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserDTO getCurrentUser(String username) {
    User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    return UserMapper.toDto(user);
  }

  public UserDTO updateUser(String username, UserCredentialsDTO dto) {
    User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

    if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
      userRepository
          .findByUsername(dto.getUsername())
          .ifPresent(
              u -> {
                throw new DuplicateUsernameException("Указанный никнейм уже используется");
              });
      user.setUsername(dto.getUsername());
    }

    if (dto.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    return UserMapper.toDto(userRepository.save(user));
  }

  public User findByUsername(String username) {
    return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
  }
}
