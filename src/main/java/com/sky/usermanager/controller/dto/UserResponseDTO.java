package com.sky.usermanager.controller.dto;

import com.sky.usermanager.model.User;

public record UserResponseDTO(Long id, String email, String name) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
