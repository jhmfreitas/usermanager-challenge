package com.sky.usermanager.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserExternalProjectKey implements Serializable {

  private String projectId;
  private Long userId;

  protected UserExternalProjectKey() {}

  public UserExternalProjectKey(String projectId, Long userId) {
    this.projectId = Objects.requireNonNull(projectId, "projectId must not be null");
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
  }

  public String getProjectId() {
    return projectId;
  }

  public Long getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserExternalProjectKey key)) return false;
    return Objects.equals(projectId, key.projectId) &&
        Objects.equals(userId, key.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectId, userId);
  }
}
