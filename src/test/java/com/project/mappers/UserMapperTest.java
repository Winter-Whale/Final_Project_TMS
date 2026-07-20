package com.project.mappers;

import com.project.models.User;
import com.project.models.dto.user.RegistrationDTO;
import com.project.models.dto.user.UserCreateDTO;
import com.project.models.dto.user.UserUpdateDTO;
import com.project.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserMapperTest {

    private UserMapper userMapper;

    private final Integer USER_ID = 1;
    private final String FULL_NAME = "John Doe";
    private final String PHONE = "+123456789";
    private final String EMAIL = "john@example.com";
    private final int AGE = 30;

    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;
    private RegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

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

        registrationDTO = new RegistrationDTO();
        registrationDTO.setFullName(FULL_NAME);
        registrationDTO.setPhone(PHONE);
        registrationDTO.setEmail(EMAIL);
        registrationDTO.setAge(AGE);
    }

    @Test
    void shouldMapCreateDTOToUserCorrectly() {
        LocalDateTime before = LocalDateTime.now();

        User user = userMapper.mapFromUserCreateDTOToUser(createDTO);

        LocalDateTime after = LocalDateTime.now();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(0);
        assertThat(user.getFullName()).isEqualTo(FULL_NAME);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getAge()).isEqualTo(AGE);
        assertThat(user.getCreated()).isBetween(before, after);
        assertThat(user.getUpdated()).isBetween(before, after);
    }

    @Test
    void shouldThrowNullPointerException_whenCreateDTOIsNull() {
        assertThatThrownBy(() -> userMapper.mapFromUserCreateDTOToUser(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMapUpdateDTOToUserCorrectly() {
        LocalDateTime before = LocalDateTime.now();

        User user = userMapper.mapFromUserUpdateRequestDTOToUser(updateDTO);

        LocalDateTime after = LocalDateTime.now();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(USER_ID);
        assertThat(user.getFullName()).isEqualTo(FULL_NAME);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getAge()).isEqualTo(AGE);
        assertThat(user.getCreated()).isNull();
        assertThat(user.getUpdated()).isBetween(before, after);
    }

    @Test
    void shouldThrowNullPointerException_whenUpdateDTOIsNull() {
        assertThatThrownBy(() -> userMapper.mapFromUserUpdateRequestDTOToUser(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMapRegistrationDTOToUserCorrectly() {
        LocalDateTime before = LocalDateTime.now();

        User user = userMapper.mapFromUserRegistrationRequestDTOToUser(registrationDTO);

        LocalDateTime after = LocalDateTime.now();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(0);
        assertThat(user.getFullName()).isEqualTo(FULL_NAME);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getAge()).isEqualTo(AGE);
        assertThat(user.getCreated()).isBetween(before, after);
        assertThat(user.getUpdated()).isBetween(before, after);
    }

    @Test
    void shouldThrowNullPointerException_whenRegistrationDTOIsNull() {
        assertThatThrownBy(() -> userMapper.mapFromUserRegistrationRequestDTOToUser(null))
                .isInstanceOf(NullPointerException.class);
    }
}