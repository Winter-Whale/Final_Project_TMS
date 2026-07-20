package com.project.mappers;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.user.RegistrationDTO;
import com.project.util.SecurityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SecurityMapperTest {

    private SecurityMapper securityMapper;
    private RegistrationDTO registrationDTO;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        securityMapper = new SecurityMapper();
        registrationDTO = new RegistrationDTO();
        registrationDTO.setUsername("john_doe");
        registrationDTO.setPassword("secret");

        user = new User();
        user.setId(1);
        user.setFullName("John Doe");

        role = Role.OWNER;
    }

    @Test
    void shouldMapRegistrationDTOToSecurityCorrectly() {
        Security security = securityMapper.mapFromRegistrationDTOToSecurity(registrationDTO, user, role);

        assertThat(security).isNotNull();
        assertThat(security.getUsername()).isEqualTo("john_doe");
        assertThat(security.getPassword()).isEqualTo("secret");
        assertThat(security.getRole()).isEqualTo(Role.OWNER);
        assertThat(security.getUser()).isSameAs(user);
    }

    @Test
    void shouldThrowNullPointerException_whenRegistrationDTOIsNull() {
        assertThatThrownBy(() -> securityMapper.mapFromRegistrationDTOToSecurity(null, user, role))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAcceptNullUserAndRole() {
        Security security = securityMapper.mapFromRegistrationDTOToSecurity(registrationDTO, null, null);

        assertThat(security).isNotNull();
        assertThat(security.getUsername()).isEqualTo("john_doe");
        assertThat(security.getPassword()).isEqualTo("secret");
        assertThat(security.getRole()).isNull();
        assertThat(security.getUser()).isNull();
    }
}
