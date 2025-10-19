package com.bmstu.lab.application.service;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.application.exception.DuplicateUsernameException;
import com.bmstu.lab.application.exception.InvalidCredentialsException;
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

  public User getById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
  }

  public UserDTO register(UserCredentialsDTO dto) {
    if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
      throw new DuplicateUsernameException("Пользователь с таким именем уже существует");
    }

    User user = UserMapper.fromAuthenticationDto(dto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return UserMapper.toDto(userRepository.save(user));
  }

  public UserDTO getCurrentUser(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    return UserMapper.toDto(user);
  }

  public UserDTO updateUser(String username, UserCredentialsDTO dto) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

    userRepository
        .findByUsername(dto.getUsername())
        .orElseThrow(() -> new DuplicateUsernameException("Указанный никнейм уже используется"));

    if (dto.getUsername() != null) user.setUsername(dto.getUsername());
    if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));

    return UserMapper.toDto(userRepository.save(user));
  }

  public User authenticate(String username, String rawPassword) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("Неверный логин или пароль"));

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new InvalidCredentialsException("Неверный логин или пароль");
    }

    return user;
  }
}
