package com.sky.usermanager.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserExternalProjectDTO(
    @NotBlank(message = "Project id is required")
    @Size(max = 200, message = "Project id cannot exceed 200 characters")
    String id,
    @NotBlank(message = "Project name is required")
    @Size(max = 120, message = "Project name cannot exceed 120 characters")
    String name
) {}
