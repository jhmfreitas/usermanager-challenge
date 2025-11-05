package com.sky.usermanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserExternalProjectKeyTest {

    public static final String PROJ_123 = "proj-123";

    @Test
    @DisplayName("Should create key successfully when valid data provided")
    void shouldCreateKeySuccessfully_WhenValidDataProvided() {
        UserExternalProjectKey key = new UserExternalProjectKey(PROJ_123, 1L);

        assertThat(key.getProjectId()).isEqualTo(PROJ_123);
        assertThat(key.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when projectId is null")
    void shouldThrowException_WhenProjectIdIsNull() {
        assertThatThrownBy(() -> new UserExternalProjectKey(null, 1L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("projectId must not be null");
    }

    @Test
    @DisplayName("Should throw exception when userId is null")
    void shouldThrowException_WhenUserIdIsNull() {
        assertThatThrownBy(() -> new UserExternalProjectKey(PROJ_123, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId must not be null");
    }

    @Test
    @DisplayName("Should consider keys equal when projectId and userId are same")
    void shouldBeEqual_WhenProjectIdAndUserIdAreSame() {
        UserExternalProjectKey key1 = new UserExternalProjectKey(PROJ_123, 1L);
        UserExternalProjectKey key2 = new UserExternalProjectKey(PROJ_123, 1L);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("Should consider keys not equal when projectId or userId differ")
    void shouldNotBeEqual_WhenProjectIdOrUserIdDiffer() {
        UserExternalProjectKey key1 = new UserExternalProjectKey(PROJ_123, 1L);
        UserExternalProjectKey key2 = new UserExternalProjectKey(PROJ_123, 2L);
        UserExternalProjectKey key3 = new UserExternalProjectKey("proj-999", 1L);

        assertThat(key1).isNotEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
    }

    @Test
    @DisplayName("Should consider keys equal when projectId and userId are the same")
    void shouldBeConsistentHashCode_forSameData() {
        UserExternalProjectKey key1 = new UserExternalProjectKey(PROJ_123, 1L);
        UserExternalProjectKey key2 = new UserExternalProjectKey(PROJ_123, 1L);

        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("Should consider keys not equal when projectId or userId differ")
    void shouldBeDifferentHashCode_forDifferentData() {
        UserExternalProjectKey key1 = new UserExternalProjectKey(PROJ_123, 1L);
        UserExternalProjectKey key2 = new UserExternalProjectKey("proj-124", 1L);
        UserExternalProjectKey key3 = new UserExternalProjectKey(PROJ_123, 2L);

        assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
        assertThat(key1.hashCode()).isNotEqualTo(key3.hashCode());
    }
}
