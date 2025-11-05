package com.sky.usermanager.model;

import org.springframework.stereotype.Component;

/**
 * Factory responsible for creating {@link UserExternalProject} instances and ensuring that
 * bidirectional relationships are established between the user and the project.
 * Moreover, it encapsulates the creation logic of projects preventing the direct instantiation
 * of {@link UserExternalProject} objects avoiding inconsistencies in the relationship between
 * users and projects.
 *
 */
@Component
public class UserExternalProjectFactory {

    public UserExternalProject create(User user, String projectId, String name) {
        UserExternalProject project = new UserExternalProject(projectId, user, name);
        user.addExternalProject(project);
        return project;
    }
}
