package com.sky.usermanager.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTO(
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 200, message = "Email cannot exceed 200 characters")
    String email,
    @NotBlank(message = "Password is required")
    @Size(max = 129, message = "Password cannot exceed 129 characters")
    String password,
    @Size(max = 120, message = "Name cannot exceed 120 characters")
    String name
) {}
