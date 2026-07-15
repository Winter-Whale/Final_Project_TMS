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
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Security testSecurity;
    private final String EXISTING_PHONE = "+123456789";
    private final String NON_EXISTING_PHONE = "+000000000";
    private final String EXISTING_USERNAME = "john_doe";
    private final String NON_EXISTING_USERNAME = "unknown";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.clear();

        testUser = new User();
        testUser.setFullName("John Doe");
        testUser.setPhone(EXISTING_PHONE);
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
    void shouldReturnTrue_whenPhoneExists() {
        boolean exists = userRepository.existsByPhone(EXISTING_PHONE);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenPhoneDoesNotExist() {
        boolean exists = userRepository.existsByPhone(NON_EXISTING_PHONE);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindUserByUsername_whenExists() {
        Optional<User> found = userRepository.findByUsername(EXISTING_USERNAME);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getPhone()).isEqualTo(EXISTING_PHONE);
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldReturnEmptyOptional_whenUsernameDoesNotExist() {
        Optional<User> found = userRepository.findByUsername(NON_EXISTING_USERNAME);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptional_whenSecurityExistsButUserDoesNot() {
    }
}