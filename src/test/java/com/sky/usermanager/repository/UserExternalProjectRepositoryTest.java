package com.sky.usermanager.repository;

import com.sky.usermanager.config.TestJpaAuditingConfig;
import com.sky.usermanager.model.User;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectFactory;
import com.sky.usermanager.model.UserExternalProjectKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestJpaAuditingConfig.class, UserExternalProjectFactory.class})
class UserExternalProjectRepositoryTest {

    public static final String PROJ_123 = "proj-123";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserExternalProjectFactory projectFactory;

    @Autowired
    private UserExternalProjectRepository projectRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("projectuser@example.com", "password", "Project User");
        userRepository.save(user);
    }

    @Test
    @DisplayName("Should save a project linked to a user")
    void shouldSaveProjectForUser() {
        UserExternalProject project = projectFactory.create(user, PROJ_123, "My Project");
        projectRepository.save(project);

        UserExternalProjectKey userExternalProjectKey = new UserExternalProjectKey(PROJ_123,
                user.getId());

        Optional<UserExternalProject> found = projectRepository.findById(userExternalProjectKey);
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isEqualTo(user);
        assertThat(found.get().getName()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("Should throw exception when creating project with null user")
    void shouldFail_WhenUserIsNull() {
        assertThatThrownBy(() -> projectFactory.create(null, PROJ_123, "No User"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("user must not be null");
    }

    @Test
    @DisplayName("Should throw exception when creating project with null name")
    void shouldFail_WhenNameIsNull() {
        assertThatThrownBy(() -> projectFactory.create(user, PROJ_123, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name must not be null");
    }
}
