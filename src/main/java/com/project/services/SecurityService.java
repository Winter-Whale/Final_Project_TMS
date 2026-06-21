package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.User.RegistrationDTO;
import com.project.models.dto.User.SecurityUpdateDTO;
import com.project.repositories.SecurityRepository;
import com.project.repositories.UserRepository;
import com.project.util.SecurityMapper;
import com.project.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final SecurityRepository securityRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityMapper securityMapper;

    public Optional<Security> getSecurityById(Integer id) {
        Optional<Security> securityFromDatabase = securityRepository.findById(id);
        return securityFromDatabase;
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public User registration(RegistrationDTO registrationDTO, Role role) throws RegistrationException {
        if (securityRepository.existsByUsername(registrationDTO.getUsername()) || userRepository.existsByPhone(registrationDTO.getPhone())) {
            throw new RegistrationException("Username/Phone already exists");
        }
        User user = userMapper.mapFromUserRegistrationRequestDTOToUser(registrationDTO);
        user = userRepository.save(user);
        Security security = securityMapper.mapFromRegistrationDTOToSecurity(registrationDTO, user, role);
        securityRepository.save(security);
        return user;
    }

    public void updateSecurity(Integer userId, SecurityUpdateDTO dto) throws UserNotFoundException, UserUpdateException {
        Security security = securityRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
        if(!dto.getCurrentPassword().equals(security.getPassword())){
            throw  new UserUpdateException("Current password is incorrect");
        }
        if (!security.getUsername().equals(dto.getNewUserName())){
            if(securityRepository.existsByUsername(dto.getNewUserName())){
                throw  new UserUpdateException("Username already taken");
            }
            security.setUsername(dto.getNewUserName());
        }
        security.setPassword(dto.getNewPassword());
        securityRepository.save(security);
    }
}

