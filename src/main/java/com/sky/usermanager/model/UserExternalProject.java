package com.sky.usermanager.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_user_external_project")
public class UserExternalProject {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "projectId", column = @Column(name = "id", nullable = false, length = 200))
    })
    private UserExternalProjectKey externalProjectKey;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected UserExternalProject() {
    }

    UserExternalProject(String projectId, User user, String name) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");

        this.externalProjectKey = new UserExternalProjectKey(
                Objects.requireNonNull(projectId, "projectId must not be null"),
                user.getId()
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return this.externalProjectKey.getProjectId();
    }

    void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserExternalProject that)) return false;
        return Objects.equals(externalProjectKey, that.externalProjectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalProjectKey);
    }

}
