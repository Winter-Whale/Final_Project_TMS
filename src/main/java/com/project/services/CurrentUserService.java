package com.project.services;

import com.project.exceptions.UserNotFoundException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.repositories.SecurityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SecurityRepository securityRepository;

    public Security getCurrentSecurity() {
        log.debug("IN CurrentUserService: getCurrentSecurity");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("OUT CurrentUserService: getCurrentSecurity");
        return securityRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    public User getCurrentUser() {
        log.debug("IN CurrentUserService: getCurrentUser");
        log.debug("OUT CurrentUserService: getCurrentUser");
        return getCurrentSecurity().getUser();
    }

    public boolean hasRole(Role role) {
        log.debug("IN CurrentUserService: hasRole");
        log.debug("OUT CurrentUserService: hasRole");
        return getCurrentSecurity().getRole() == role;
    }
}
