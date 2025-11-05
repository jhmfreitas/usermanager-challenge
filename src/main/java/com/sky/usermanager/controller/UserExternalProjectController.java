package com.sky.usermanager.controller;

import com.sky.usermanager.controller.dto.UserExternalProjectDTO;
import com.sky.usermanager.controller.dto.UserExternalProjectResponseDTO;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.service.ExternalProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/projects")
public class UserExternalProjectController {

    private static final Logger log = LoggerFactory.getLogger(UserExternalProjectController.class);

    private final ExternalProjectService externalProjectService;

    public UserExternalProjectController(ExternalProjectService externalProjectService) {
        this.externalProjectService = externalProjectService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<UserExternalProjectResponseDTO> getUserProjectById(@PathVariable Long userId,
                                                                             @PathVariable String projectId) {
        log.info("Received request to get external project [userId={}, projectId={}]", userId, projectId);
        UserExternalProject project = externalProjectService.getUserProjectById(userId, projectId);
        log.info("Returning project [userId={}, projectId={}, name={}]", userId, project.getId(), project.getName());

        return ResponseEntity.ok(UserExternalProjectResponseDTO.from(project));
    }

    @PostMapping
    public ResponseEntity<UserExternalProjectResponseDTO> addExternalProject(
            @PathVariable Long userId,
            @Valid @RequestBody UserExternalProjectDTO projectDTO, UriComponentsBuilder uriBuilder) {
        log.info("Received request to add external project [userId={}, projectId={}, name={}]",
                userId, projectDTO.id(), projectDTO.name());

        UserExternalProject createdProject = externalProjectService.addExternalProject(userId, projectDTO);

        URI location = uriBuilder.path("/api/users/{userId}/projects/{createdProjectId}")
                .buildAndExpand(userId, createdProject.getId())
                .toUri();

        log.info("External project successfully created [userId={}, projectId={}, location={}]",
                userId, createdProject.getId(), location);

        return ResponseEntity.created(location).body(UserExternalProjectResponseDTO.from(createdProject));
    }

    @GetMapping
    public ResponseEntity<List<UserExternalProjectResponseDTO>> getUserProjects(@PathVariable Long userId) {
        log.info("Received request to list external projects [userId={}]", userId);

        List<UserExternalProject> projects = externalProjectService.getUserProjects(userId);

        List<UserExternalProjectResponseDTO> projectDtos = projects.stream()
                .map(UserExternalProjectResponseDTO::from)
                .toList();

        log.info("Returning {} external projects for user [userId={}]", projectDtos.size(), userId);

        return ResponseEntity.ok(projectDtos);
    }

}
