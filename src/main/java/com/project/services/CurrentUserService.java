package com.project.services;

import com.project.exceptions.UserNotFoundException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.repositories.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SecurityRepository securityRepository;

    public Security getCurrentSecurity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return securityRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    public User getCurrentUser() {
        return getCurrentSecurity().getUser();
    }

    public boolean hasRole(Role role) {
        return getCurrentSecurity().getRole() == role;
    }
}
