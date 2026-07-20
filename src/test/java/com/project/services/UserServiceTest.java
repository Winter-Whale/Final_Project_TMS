package com.project.services;

import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.User;
import com.project.models.dto.user.UserCreateDTO;
import com.project.models.dto.user.UserUpdateDTO;
import com.project.repositories.UserRepository;
import com.project.util.UserMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final Integer USER_ID = 1;
    private final String USERNAME = "john_doe";
    private final String PHONE = "+123456789";
    private final String NEW_PHONE = "+987654321";
    private final String FULL_NAME = "John Doe";
    private final String EMAIL = "john@example.com";
    private final int AGE = 30;

    private User user;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setPhone(PHONE);
        user.setFullName(FULL_NAME);
        user.setEmail(EMAIL);
        user.setAge(AGE);

        createDTO = new UserCreateDTO();
        createDTO.setFullName(FULL_NAME);
        createDTO.setPhone(PHONE);
        createDTO.setEmail(EMAIL);
        createDTO.setAge(AGE);

        updateDTO = new UserUpdateDTO();
        updateDTO.setId(USER_ID);
        updateDTO.setFullName(FULL_NAME);
        updateDTO.setPhone(PHONE);
        updateDTO.setEmail(EMAIL);
        updateDTO.setAge(AGE);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(user);
        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnUser_whenExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(USER_ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldReturnEmptyOptional_whenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(USER_ID);

        assertThat(result).isEmpty();
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldReturnCurrentUser_whenAuthenticated() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            Optional<User> result = userService.getInfoAboutMyself();

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(user);
            verify(userRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldReturnEmptyOptional_whenUserNotFoundByUsername() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(USERNAME);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            Optional<User> result = userService.getInfoAboutMyself();

            assertThat(result).isEmpty();
            verify(userRepository).findByUsername(USERNAME);
        }
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userMapper.mapFromUserCreateDTOToUser(createDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(createDTO);

        assertThat(result).isEqualTo(user);
        verify(userMapper).mapFromUserCreateDTOToUser(createDTO);
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateUserSuccessfully_whenPhoneNotChangedOrUnique() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        // Явно заглушаем existsByPhone, т.к. метод вызывается всегда, но возвращает false
        when(userRepository.existsByPhone(PHONE)).thenReturn(false);
        when(userMapper.mapFromUserUpdateRequestDTOToUser(updateDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(updateDTO);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(USER_ID);
        verify(userRepository).existsByPhone(PHONE);
        verify(userMapper).mapFromUserUpdateRequestDTOToUser(updateDTO);
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateUserSuccessfully_whenPhoneChangedAndUnique() {
        updateDTO.setPhone(NEW_PHONE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(NEW_PHONE)).thenReturn(false);
        when(userMapper.mapFromUserUpdateRequestDTOToUser(updateDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(updateDTO);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(USER_ID);
        verify(userRepository).existsByPhone(NEW_PHONE);
        verify(userMapper).mapFromUserUpdateRequestDTOToUser(updateDTO);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserNotFoundForUpdate() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(updateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Sorry, user not found");
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserUpdateException_whenPhoneAlreadyTakenByAnotherUser() {
        updateDTO.setPhone(NEW_PHONE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(NEW_PHONE)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(updateDTO))
                .isInstanceOf(UserUpdateException.class)
                .hasMessage("Phone already taken by another user");
        verify(userRepository).findById(USER_ID);
        verify(userRepository).existsByPhone(NEW_PHONE);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserSuccessfully_whenExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(USER_ID);

        userService.deleteUserById(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserNotFoundForDelete() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Sorry, user not found");
        verify(userRepository, never()).deleteById(anyInt());
    }
}