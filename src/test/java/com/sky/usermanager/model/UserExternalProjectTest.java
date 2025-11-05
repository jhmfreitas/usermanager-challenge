package com.sky.usermanager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sky.usermanager.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserExternalProjectTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(TEST_EXAMPLE_EMAIL, HASHED_PASSWORD_123, JOHN_DOE_NAME);
        setId(user, 1L);
    }

    @Test
    @DisplayName("Should create a new project")
    void shouldInitializeProjectCorrectly_WhenValidDataProvided() {
        UserExternalProject project = new UserExternalProject("proj-123", user, "Project One");
        user.addExternalProject(project);

        assertThat(project.getId()).isEqualTo("proj-123");
        assertThat(project.getName()).isEqualTo("Project One");
        assertThat(project.getUser()).isEqualTo(user);
        assertThat(user.getExternalProjects()).contains(project);

        assertThat(project.getCreatedAt()).isNull();
        assertThat(project.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when projectId is empty")
    void shouldThrowException_WhenProjectIdIsNull() {
        assertThatThrownBy(() -> new UserExternalProject(null, user, "Project One"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("projectId must not be null");
    }

    @Test
    @DisplayName("Should throw exception when user is null")
    void shouldThrowException_WhenUserIsNull() {
        assertThatThrownBy(() -> new UserExternalProject("proj-123", null, "Project One"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("user must not be null");
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowException_WhenNameIsNull() {
        assertThatThrownBy(() -> new UserExternalProject("proj-123", user, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name must not be null");
    }

    @Test
    @DisplayName("Should update project name correctly")
    void shouldUpdateNameCorrectly_WhenSetNameCalled() {
        UserExternalProject project = new UserExternalProject("proj-123", user, "Project One");
        project.setName("Updated Project");

        assertThat(project.getName()).isEqualTo("Updated Project");
    }

    @Test
    @DisplayName("Should link project to user")
    void shouldLinkProjectToUser_WhenCreated() {
        UserExternalProject project = new UserExternalProject("proj-123", user, "Project One");
        user.addExternalProject(project);

        List<UserExternalProject> projects = user.getExternalProjects();
        assertThat(projects).hasSize(1).contains(project);
    }

    @Test
    @DisplayName("Should consider projects equal when keys are the same")
    void shouldBeEqual_WhenKeysAreEqual() {
        UserExternalProject project1 = new UserExternalProject("proj-123", user, "Project One");
        UserExternalProject project2 = new UserExternalProject("proj-123", user, "Another Name");

        // keys are the same => equals true
        assertThat(project1).isEqualTo(project2);
        assertThat(project1.hashCode()).isEqualTo(project2.hashCode());
    }

    @Test
    @DisplayName("Should consider projects not equal when keys are different")
    void shouldNotBeEqual_WhenKeysDiffer() {
        UserExternalProject project1 = new UserExternalProject("proj-123", user, "Project One");
        UserExternalProject project2 = new UserExternalProject("proj-456", user, "Project One");

        assertThat(project1).isNotEqualTo(project2);
    }
}
