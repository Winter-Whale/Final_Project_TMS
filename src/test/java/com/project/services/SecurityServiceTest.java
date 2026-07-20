package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.user.AuthRequestDTO;
import com.project.models.dto.user.AuthResponseDTO;
import com.project.models.dto.user.RegistrationDTO;
import com.project.models.dto.user.SecurityUpdateDTO;
import com.project.repositories.SecurityRepository;
import com.project.repositories.UserRepository;
import com.project.util.SecurityMapper;
import com.project.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityMapper securityMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityService securityService;

    private final Integer USER_ID = 1;
    private final String USERNAME = "john_doe";
    private final String PASSWORD = "encoded_password";
    private final String PLAIN_PASSWORD = "secret";
    private final String NEW_USERNAME = "new_john";
    private final String NEW_PASSWORD = "new_encoded_password";
    private final String PHONE = "+123456789";

    private User user;
    private Security security;
    private RegistrationDTO registrationDTO;
    private SecurityUpdateDTO updateDTO;
    private AuthRequestDTO authRequestDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setPhone(PHONE);

        security = new Security();
        security.setId(USER_ID);
        security.setUsername(USERNAME);
        security.setPassword(PASSWORD);
        security.setUser(user);

        registrationDTO = new RegistrationDTO();
        registrationDTO.setUsername(USERNAME);
        registrationDTO.setPassword(PLAIN_PASSWORD);
        registrationDTO.setPhone(PHONE);

        updateDTO = new SecurityUpdateDTO();
        updateDTO.setCurrentPassword(PASSWORD);
        updateDTO.setNewUserName(USERNAME);
        updateDTO.setNewPassword(NEW_PASSWORD);

        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername(USERNAME);
        authRequestDTO.setPassword(PLAIN_PASSWORD);
    }

    @Test
    void shouldReturnSecurity_whenExists() {
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.of(security));

        Optional<Security> result = securityService.getSecurityById(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(security);
        verify(securityRepository).findById(USER_ID);
    }

    @Test
    void shouldReturnEmptyOptional_whenSecurityNotFound() {
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<Security> result = securityService.getSecurityById(USER_ID);

        assertThat(result).isEmpty();
        verify(securityRepository).findById(USER_ID);
    }

    @Test
    void shouldRegisterSuccessfully_whenUsernameAndPhoneAreUnique() {
        when(securityRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByPhone(PHONE)).thenReturn(false);
        when(userMapper.mapFromUserRegistrationRequestDTOToUser(registrationDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(securityMapper.mapFromRegistrationDTOToSecurity(registrationDTO, user, Role.OWNER))
                .thenReturn(security);
        when(securityRepository.save(any(Security.class))).thenReturn(security);

        User result = securityService.registration(registrationDTO, Role.OWNER);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
        verify(securityRepository).save(security);
    }

    @Test
    void shouldThrowRegistrationException_whenUsernameExists() {
        when(securityRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> securityService.registration(registrationDTO, Role.OWNER))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Username/Phone already exists");
        verify(userRepository, never()).save(any());
        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldThrowRegistrationException_whenPhoneExists() {
        when(securityRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByPhone(PHONE)).thenReturn(true);

        assertThatThrownBy(() -> securityService.registration(registrationDTO, Role.OWNER))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Username/Phone already exists");
        verify(userRepository, never()).save(any());
        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSecuritySuccessfully_whenAllValidAndUsernameUnchanged() {
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.of(security));
        when(securityRepository.save(any(Security.class))).thenReturn(security);

        securityService.updateSecurity(USER_ID, updateDTO);

        ArgumentCaptor<Security> captor = ArgumentCaptor.forClass(Security.class);
        verify(securityRepository).save(captor.capture());
        Security saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(USERNAME); // не изменился
        assertThat(saved.getPassword()).isEqualTo(NEW_PASSWORD);
        verify(securityRepository, never()).existsByUsername(anyString());
    }

    @Test
    void shouldUpdateSecuritySuccessfully_whenUsernameChangedAndNewUsernameIsUnique() {
        updateDTO.setNewUserName(NEW_USERNAME);
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.of(security));
        when(securityRepository.existsByUsername(NEW_USERNAME)).thenReturn(false);
        when(securityRepository.save(any(Security.class))).thenReturn(security);

        securityService.updateSecurity(USER_ID, updateDTO);

        ArgumentCaptor<Security> captor = ArgumentCaptor.forClass(Security.class);
        verify(securityRepository).save(captor.capture());
        Security saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(NEW_USERNAME);
        assertThat(saved.getPassword()).isEqualTo(NEW_PASSWORD);
        verify(securityRepository).existsByUsername(NEW_USERNAME);
    }

    @Test
    void shouldThrowUserNotFoundException_whenSecurityNotFound() {
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> securityService.updateSecurity(USER_ID, updateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserUpdateException_whenCurrentPasswordIncorrect() {
        updateDTO.setCurrentPassword("wrong_password");
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.of(security));

        assertThatThrownBy(() -> securityService.updateSecurity(USER_ID, updateDTO))
                .isInstanceOf(UserUpdateException.class)
                .hasMessage("Current password is incorrect");
        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserUpdateException_whenNewUsernameAlreadyTaken() {
        updateDTO.setNewUserName(NEW_USERNAME);
        when(securityRepository.findById(USER_ID)).thenReturn(Optional.of(security));
        when(securityRepository.existsByUsername(NEW_USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> securityService.updateSecurity(USER_ID, updateDTO))
                .isInstanceOf(UserUpdateException.class)
                .hasMessage("Username already taken");
        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldReturnAuthResponse_whenCredentialsAreValid() {
        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD)).thenReturn(true);
        String jwt = "some.jwt.token";
        when(jwtService.generateJWT(USERNAME)).thenReturn(jwt);

        Optional<AuthResponseDTO> result = securityService.generateJWT(authRequestDTO);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(jwt);
        verify(securityRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD);
        verify(jwtService).generateJWT(USERNAME);
    }

    @Test
    void shouldThrowUsernameNotFoundException_whenUsernameNotFound() {
        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> securityService.generateJWT(authRequestDTO))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username not found: " + USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateJWT(anyString());
    }

    @Test
    void shouldReturnEmptyOptional_whenPasswordDoesNotMatch() {
        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD)).thenReturn(false);

        Optional<AuthResponseDTO> result = securityService.generateJWT(authRequestDTO);

        assertThat(result).isEmpty();
        verify(passwordEncoder).matches(PLAIN_PASSWORD, PASSWORD);
        verify(jwtService, never()).generateJWT(anyString());
    }

    @Test
    void shouldReturnEmptyOptional_whenJwtServiceReturnsNull() {
        when(securityRepository.findByUsername(USERNAME)).thenReturn(Optional.of(security));
        when(passwordEncoder.matches(PLAIN_PASSWORD, PASSWORD)).thenReturn(true);
        when(jwtService.generateJWT(USERNAME)).thenReturn(null);

        Optional<AuthResponseDTO> result = securityService.generateJWT(authRequestDTO);

        assertThat(result).isEmpty();
        verify(jwtService).generateJWT(USERNAME);
    }
}
