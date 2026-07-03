package com.project.services;

import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.dto.User.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import com.project.models.User;
import com.project.models.dto.User.UserCreateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.project.repositories.UserRepository;
import com.project.util.UserMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        log.debug("IN UserService:getUserById");
        Optional<User> userFromDatabase = userRepository.findById(id);
        log.debug("OUT UserService:getUserById");
        return userFromDatabase;
    }


    public User createUser(UserCreateDTO userDto) {
        log.debug("IN UserService:createUser");
        User saveUser = userRepository.save(userMapper.mapFromUserCreateDTOToUser(userDto));
        log.debug("OUT UserService:createUser");
        return saveUser;
    }

    public User updateUser(UserUpdateDTO updateDTO) throws UserUpdateException, UserNotFoundException {
        log.debug("IN UserService:updateUser");
        Optional<User> userFromDatabase = userRepository.findById(updateDTO.getId());
        if (userFromDatabase.isEmpty()) {
            throw new UserNotFoundException("Sorry, user not found");
        }
        if (userRepository.existsByPhone(updateDTO.getPhone()) &&
                !userFromDatabase.get().getPhone().equals(updateDTO.getPhone())) {
            throw new UserUpdateException("Phone already taken by another user");
        }
        User updateUser = userRepository.save(userMapper.mapFromUserUpdateRequestDTOToUser(updateDTO));
        log.info("User with id: {} updated", updateDTO.getId());
        log.debug("OUT UserService:updateUser");
        return updateUser;
    }

    public void deleteUserById(Integer id) throws UserNotFoundException {
        log.debug("IN UserService:deleteUserById");
        Optional<User> userFromDatabase = userRepository.findById(id);
        if (userFromDatabase.isEmpty()) {
            throw new UserNotFoundException("Sorry, user not found");
        }
        userRepository.deleteById(id);
        log.info("Delete user with id: {}", id);
        log.debug("OUT UserService:deleteUserById");
    }
}
