package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.UserDTO;
import com.bmstu.lab.infrastructure.persistence.entity.User;

public class UserMapper {

  public static UserDTO toDto(User user) {
    return new UserDTO(user.getId(), user.getUsername(), user.isModerator());
  }

  public static User toEntity(UserDTO dto) {
    User user = new User();
    user.setUsername(dto.getUsername());
    user.setModerator(false);
    return user;
  }
}
