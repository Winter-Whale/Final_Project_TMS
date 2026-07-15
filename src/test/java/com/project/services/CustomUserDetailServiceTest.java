package com.project.services;

import com.project.models.Role;
import com.project.models.Security;
import com.project.repositories.SecurityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {
    @Mock
    private SecurityRepository securityRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    private final String USERNAME = "john_doe";
    private final String PASSWORD = "encoded_password";

    @Test
    void shouldLoadUserDetails_whenUserExists() {
        Security security = new Security();
        security.setUsername(USERNAME);
        security.setPassword(PASSWORD);
        security.setRole(Role.OWNER);

        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));

        UserDetails userDetails = customUserDetailService.loadUserByUsername(USERNAME);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
        assertThat(userDetails.getPassword()).isEqualTo(PASSWORD);
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_OWNER");

        verify(securityRepository).findByUsername(USERNAME);
    }

    @Test
    void shouldThrowUsernameNotFoundException_whenUserNotFound() {
        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(USERNAME);

        verify(securityRepository).findByUsername(USERNAME);
    }
}

