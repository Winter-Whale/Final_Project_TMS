package com.project.services;

import com.project.exceprions.UserNotFoundException;
import com.project.exceprions.UserUpdateException;
import com.project.models.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import com.project.models.User;
import com.project.models.dto.UserCreateDTO;
import org.springframework.stereotype.Service;
import com.project.repositories.UserRepository;
import com.project.util.UserMapper;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        Optional<User> userFromDatabase = userRepository.findById(id);
        return userFromDatabase;
    }


    public User createUser(UserCreateDTO userDto) {
        User saveUser = userRepository.save(userMapper.mapFromUserCreateDTOToUser(userDto));
        return saveUser;
    }

    public User updateUser(UserUpdateDTO updateDTO) throws UserUpdateException, UserNotFoundException {
        Optional<User> userFromDatabase = userRepository.findById(updateDTO.getId());
        if (userFromDatabase.isEmpty()) {
            throw new UserNotFoundException("Sorry, user not found");
        }
        if (userRepository.existsByPhone(updateDTO.getPhone()) &&
                !userFromDatabase.get().getPhone().equals(updateDTO.getPhone())) {
            throw new UserUpdateException("Phone already taken by another user");
        }
        User updateUser = userRepository.save(userMapper.mapFromUserUpdateRequestDTOToUser(updateDTO));
        return updateUser;
    }

    public void deleteUserById(Integer id) throws UserNotFoundException {
        Optional<User> userFromDatabase = userRepository.findById(id);
        if (userFromDatabase.isEmpty()) {
            throw new UserNotFoundException("Sorry, user not found");
        }
        userRepository.deleteById(id);
    }
}
