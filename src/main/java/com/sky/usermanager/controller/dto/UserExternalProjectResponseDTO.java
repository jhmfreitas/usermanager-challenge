package com.sky.usermanager.controller.dto;

import com.sky.usermanager.model.UserExternalProject;

public record UserExternalProjectResponseDTO(String id, Long userId, String projectName) {

    public static UserExternalProjectResponseDTO from(UserExternalProject externalProject) {
        return new UserExternalProjectResponseDTO(
                externalProject.getId(),
                externalProject.getUser().getId(),
                externalProject.getName()
        );
    }
}
