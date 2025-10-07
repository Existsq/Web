package com.bmstu.lab.user.service;

import com.bmstu.lab.user.exception.DuplicateUserException;
import com.bmstu.lab.user.exception.InvalidCredentialsException;
import com.bmstu.lab.user.exception.UserNotFoundException;
import com.bmstu.lab.user.model.dto.UserDTO;
import com.bmstu.lab.user.model.dto.UserRegistrationDTO;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.model.mapper.UserMapper;
import com.bmstu.lab.user.repository.UserRepository;
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

  public UserDTO register(UserRegistrationDTO dto) {
    if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
      throw new DuplicateUserException("Пользователь с таким именем уже существует");
    }

    User user = UserMapper.fromRegistrationDto(dto);
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

  public UserDTO updateUser(String username, UserRegistrationDTO dto) {
    username = "new_user";
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

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
