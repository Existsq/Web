package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.UserCredentialsDTO;
import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.infrastructure.persistence.entity.User;

public class UserMapper {

  public static UserDTO toDto(User user) {
    return new UserDTO(user.getId(), user.getUsername(), user.isModerator());
  }

  public static User toEntity(UserCredentialsDTO dto) {
    User user = new User();
    user.setUsername(dto.getUsername());
    user.setPassword(dto.getPassword());
    user.setModerator(false);
    return user;
  }
}
