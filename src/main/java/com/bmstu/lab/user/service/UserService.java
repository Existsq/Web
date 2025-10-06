package com.bmstu.lab.user.service;

import com.bmstu.lab.user.exception.UserNotFoundException;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
  }
}
