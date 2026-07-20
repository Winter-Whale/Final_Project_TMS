package com.project.services;

import com.project.models.Security;
import com.project.repositories.SecurityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final SecurityRepository securityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("IN CustomUserDetailService: loadUserByUsername");
        Optional<Security> security = securityRepository.findByUsername(username);
        if (security.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        Security securityEntity = security.get();
        log.debug("OUT CustomUserDetailService: loadUserByUsername");
        return User
                .withUsername(securityEntity.getUsername())
                .password(securityEntity.getPassword())
                .roles(securityEntity.getRole().toString())
                .build();
    }
}
