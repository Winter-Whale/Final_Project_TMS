package com.project.util;

import com.project.models.User;
import com.project.models.dto.RegistrationDTO;
import com.project.models.dto.UserCreateDTO;
import com.project.models.dto.UserUpdateDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User mapFromUserCreateDTOToUser(UserCreateDTO userDTO) {
        if (userDTO == null) {
            throw new NullPointerException();
        }
        User user = new User();
        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }

    public User mapFromUserUpdateRequestDTOToUser(UserUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new NullPointerException();
        }
        User user = new User();
        user.setId(updateDTO.getId());
        user.setFullName(updateDTO.getFullName());
        user.setPhone(updateDTO.getPhone());
        user.setEmail(updateDTO.getEmail());
        user.setAge(updateDTO.getAge());
        user.setUpdated(LocalDateTime.now());
        return user;
    }

    public User mapFromUserRegistrationRequestDTOToUser(RegistrationDTO regDTO) {
        if (regDTO == null) {
            throw new NullPointerException();
        }
        User user = new User();
        user.setFullName(regDTO.getFullName());
        user.setPhone(regDTO.getPhone());
        user.setEmail(regDTO.getEmail());
        user.setAge(regDTO.getAge());
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }
}
