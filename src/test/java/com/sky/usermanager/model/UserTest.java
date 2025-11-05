package com.sky.usermanager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sky.usermanager.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UserTest {

    public static final String JANE_DOE_NAME = "Jane Doe";
    public static final String NEW_HASHED_PASSWORD = "newHashedPassword";
    public static final String NEW_EXAMPLE_EMAIL = "new@example.com";
    public static final String ANOTHER_EXAMPLE_EMAIL = "another@example.com";
    public static final String SOMEONE_NAME = "Someone";
    public static final String PWD = "pwd";
    public static final String NAME = "Name";

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(TEST_EXAMPLE_EMAIL, HASHED_PASSWORD_123, JOHN_DOE_NAME);
    }

    @Test
    @DisplayName("Should create a new user and return null id")
    void shouldInitializeUserCorrectly_WhenCreatedWithValidData() {
        assertThat(user.getEmail()).isEqualTo(TEST_EXAMPLE_EMAIL);
        assertThat(user.getPassword()).isEqualTo(HASHED_PASSWORD_123);
        assertThat(user.getName()).isEqualTo(JOHN_DOE_NAME);
        assertThat(user.getExternalProjects()).isEmpty();
        assertThat(user.getId()).isNull();
    }

    @Test
    @DisplayName("Should update fields correctly when setters are called")
    void shouldUpdateFieldsCorrectly_WhenSettersAreCalled() {
        user.setEmail(NEW_EXAMPLE_EMAIL);
        user.setName(JANE_DOE_NAME);
        user.setPassword(NEW_HASHED_PASSWORD);

        assertThat(user.getEmail()).isEqualTo(NEW_EXAMPLE_EMAIL);
        assertThat(user.getName()).isEqualTo(JANE_DOE_NAME);
        assertThat(user.getPassword()).isEqualTo(NEW_HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should update audit fields correctly when user is persisted")
    void shouldConsiderUsersEqual_WhenIdsAreEqual() {
        User sameUser = new User(ANOTHER_EXAMPLE_EMAIL, PWD, SOMEONE_NAME);
        setId(user, 1L);
        setId(sameUser, 1L);

        assertThat(user).isEqualTo(sameUser);
        assertThat(user.hashCode()).isEqualTo(sameUser.hashCode());
    }

    @Test
    @DisplayName("Should consider users not equal when Ids are different")
    void shouldConsiderUsersNotEqual_WhenIdsAreDifferent() {
        User otherUser = new User(ANOTHER_EXAMPLE_EMAIL, PWD, SOMEONE_NAME);
        setId(user, 1L);
        setId(otherUser, 2L);

        assertThat(user).isNotEqualTo(otherUser);
    }

    @Test
    @DisplayName("Should manage external projects correctly")
    void shouldManageExternalProjectsCorrectly_WhenProjectsAddedOrRemoved() {
        UserExternalProject project1 = mock(UserExternalProject.class);
        UserExternalProject project2 = mock(UserExternalProject.class);

        user.addExternalProject(project1);
        user.addExternalProject(project2);

        List<UserExternalProject> projects = user.getExternalProjects();
        assertThat(projects).hasSize(2).contains(project1, project2);

        user.removeExternalProject(project1);
        assertThat(user.getExternalProjects()).hasSize(1).contains(project2);
    }

    @Test
    @DisplayName("Should have null audit fields when user is not persisted")
    void shouldHaveNullAuditFields_WhenUserNotPersisted() {
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when email or password is null")
    void shouldThrowException_WhenEmailOrPasswordIsNullOnCreation() {
        assertThatThrownBy(() -> new User(null, PWD, NAME))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new User(TEST_EXAMPLE_EMAIL, null, NAME))
                .isInstanceOf(NullPointerException.class);
    }

}
