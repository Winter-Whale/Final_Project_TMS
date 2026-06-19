package com.project.util;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.RegistrationDTO;
import org.springframework.stereotype.Component;


@Component
public class SecurityMapper {
    public Security mapFromRegistrationDTOToSecurity(RegistrationDTO userDTO, User user, Role role) {
        if (userDTO == null) {
            throw new NullPointerException();
        }
        Security security = new Security();
        security.setUsername(userDTO.getUsername());
        security.setPassword(userDTO.getPassword());
        security.setRole(role);
        security.setUser(user);
        return security;
    }
}
