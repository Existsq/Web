package com.bmstu.lab.user.model.mapper;

import com.bmstu.lab.user.model.dto.UserDTO;
import com.bmstu.lab.user.model.dto.UserRegistrationDTO;
import com.bmstu.lab.user.model.entity.User;

public class UserMapper {

    public static UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.isModerator());
    }

    public static User fromRegistrationDto(UserRegistrationDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // лучше хешировать
        user.setModerator(false);
        return user;
    }
}