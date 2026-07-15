package com.project.services;

import com.project.exceptions.UserNotFoundException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.repositories.SecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrentUserServiceTest {
    @Mock
    private SecurityRepository securityRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private final String USERNAME = "john_doe";
    private Security security;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setFullName("John Doe");

        security = new Security();
        security.setId(100);
        security.setUsername(USERNAME);
        security.setUser(user);
        security.setRole(Role.OWNER);
    }

    @Test
    void shouldReturnSecurity_whenAuthenticatedUserExists() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));

            Security result = currentUserService.getCurrentSecurity();

            assertThat(result).isEqualTo(security);
            verify(securityRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldThrowUserNotFoundException_whenUsernameNotFoundInRepository() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> currentUserService.getCurrentSecurity())
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Authenticated user not found");
            verify(securityRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldReturnUser_whenAuthenticated() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));

            User result = currentUserService.getCurrentUser();

            assertThat(result).isEqualTo(user);
            verify(securityRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldThrowUserNotFoundException_whenNoSecurityForUsername() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> currentUserService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Authenticated user not found");
            verify(securityRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldReturnTrue_whenRoleMatches() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));

            boolean resultOwner = currentUserService.hasRole(Role.OWNER);
            assertThat(resultOwner).isTrue();

            boolean resultRenter = currentUserService.hasRole(Role.RENTER);
            assertThat(resultRenter).isFalse();

            verify(securityRepository, times(2)).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldThrowUserNotFoundException_whenRoleCheckFailsDueToMissingSecurity() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> currentUserService.hasRole(Role.OWNER))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Authenticated user not found");
            verify(securityRepository).findByUsername(USERNAME);
        }
    }
}
