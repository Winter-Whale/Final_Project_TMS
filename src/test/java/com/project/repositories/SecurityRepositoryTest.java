package com.project.repositories;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SecurityRepositoryTest {
    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Security testSecurity;
    private final String EXISTING_USERNAME = "john_doe";
    private final String NON_EXISTING_USERNAME = "unknown_user";

    @BeforeEach
    void setUp() {
        securityRepository.deleteAll();
        entityManager.clear();

        testUser = new User();
        testUser.setFullName("John Doe");
        testUser.setPhone("+123456789");
        testUser.setEmail("john@example.com");
        testUser.setAge(30);
        testUser.setCreated(LocalDateTime.now());
        testUser.setUpdated(LocalDateTime.now());
        entityManager.persist(testUser);
        entityManager.flush();

        testSecurity = new Security();
        testSecurity.setUser(testUser);
        testSecurity.setUsername(EXISTING_USERNAME);
        testSecurity.setPassword("encoded_password");
        testSecurity.setRole(Role.OWNER);
        entityManager.persist(testSecurity);
        entityManager.flush();
    }

    @Test
    void shouldReturnTrue_whenUsernameExists() {
        boolean exists = securityRepository.existsByUsername(EXISTING_USERNAME);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUsernameDoesNotExist() {
        boolean exists = securityRepository.existsByUsername(NON_EXISTING_USERNAME);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindSecurityByUsername_whenExists() {
        Optional<Security> found = securityRepository.findByUsername(EXISTING_USERNAME);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(EXISTING_USERNAME);
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void shouldReturnEmptyOptional_whenUsernameDoesNotExist() {
        Optional<Security> found = securityRepository.findByUsername(NON_EXISTING_USERNAME);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindSecurityByUsername_caseSensitive() {
        String upperCaseUsername = EXISTING_USERNAME.toUpperCase();
        Optional<Security> found = securityRepository.findByUsername(upperCaseUsername);
        assertThat(found).isEmpty();
    }
}
