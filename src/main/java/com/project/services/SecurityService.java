package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.User.AuthRequestDTO;
import com.project.models.dto.User.AuthResponseDTO;
import com.project.models.dto.User.RegistrationDTO;
import com.project.models.dto.User.SecurityUpdateDTO;
import com.project.repositories.SecurityRepository;
import com.project.repositories.UserRepository;
import com.project.util.SecurityMapper;
import com.project.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final SecurityRepository securityRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityMapper securityMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Optional<Security> getSecurityById(Integer id) {
        log.debug("IN SecurityService: getSecurityById");
        Optional<Security> securityFromDatabase = securityRepository.findById(id);
        log.debug("OUT SecurityService: getSecurityById");
        return securityFromDatabase;
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public User registration(RegistrationDTO registrationDTO, Role role) {
        log.debug("IN SecurityService: registration");
        if (securityRepository.existsByUsername(registrationDTO.getUsername())
                || userRepository.existsByPhone(registrationDTO.getPhone())) {
            throw new RegistrationException("Username/Phone already exists");
        }
        User user = userMapper.mapFromUserRegistrationRequestDTOToUser(registrationDTO);
        user = userRepository.save(user);
        log.info("User saved: {}", user.getId());
        Security security = securityMapper.mapFromRegistrationDTOToSecurity(registrationDTO, user, role);
        securityRepository.save(security);
        log.info("User security added for user with id: {}", user.getId());
        log.debug("OUT SecurityService: registration");
        return user;
    }

    public void updateSecurity(Integer userId, SecurityUpdateDTO dto) {
        log.debug("IN SecurityService: updateSecurity");
        Security security = securityRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
        if(!dto.getCurrentPassword().equals(security.getPassword())){
            throw  new UserUpdateException("Current password is incorrect");
        }
        if (!security.getUsername().equals(dto.getNewUserName())) {
            if (securityRepository.existsByUsername(dto.getNewUserName())) {
                throw new UserUpdateException("Username already taken");
            }
            security.setUsername(dto.getNewUserName());
        }
        security.setPassword(dto.getNewPassword());
        securityRepository.save(security);
        log.info("Security updated for id: {}", userId);
        log.debug("OUT SecurityService: updateSecurity");
    }

    public Optional<AuthResponseDTO> generateJWT(AuthRequestDTO authRequestDTO) {
        log.debug("IN SecurityService:generateJWT");
        Optional<Security> securityOptional = securityRepository.findByUsername(authRequestDTO.getUsername());
        if (securityOptional.isEmpty()) {
            throw new UsernameNotFoundException("Username not found: " + authRequestDTO.getUsername());
        }
        Security security = securityOptional.get();
        if (!passwordEncoder.matches(authRequestDTO.getPassword(), security.getPassword())) {
            return Optional.empty();
        }
        String jwt = jwtService.generateJWT(security.getUsername());
        if (jwt == null) {
            return Optional.empty();
        }
        log.debug("OUT SecurityService:generateJWT");
        return Optional.of(new AuthResponseDTO(jwt));
    }
}
