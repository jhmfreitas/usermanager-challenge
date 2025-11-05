package com.sky.usermanager.repository;

import com.sky.usermanager.config.TestJpaAuditingConfig;
import com.sky.usermanager.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.sky.usermanager.TestUtil.JOHN_DOE_NAME;
import static com.sky.usermanager.TestUtil.TEST_EXAMPLE_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and retrieve a user successfully")
    void shouldSaveAndRetrieveUser() {
        User user = new User(TEST_EXAMPLE_EMAIL, "password123", JOHN_DOE_NAME);
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(TEST_EXAMPLE_EMAIL);
        assertThat(found.get().getName()).isEqualTo(JOHN_DOE_NAME);
    }

    @Test
    @DisplayName("Should enforce unique email constraint at DB level")
    void shouldEnforceUniqueEmail() {
        User user1 = new User("unique@example.com", "pwd", "Alice");
        userRepository.save(user1);

        User user2 = new User("unique@example.com", "pwd", "Bob");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> userRepository.save(user2), "Should throw exception");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUser() {
        User user = new User("delete@example.com", "pwd", "To Delete");
        User saved = userRepository.save(user);

        userRepository.delete(saved);
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isNotPresent();
    }
}
