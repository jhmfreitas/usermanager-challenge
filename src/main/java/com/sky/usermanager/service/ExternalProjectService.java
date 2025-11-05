package com.sky.usermanager.service;

import com.sky.usermanager.controller.dto.UserExternalProjectDTO;
import com.sky.usermanager.exception.ResourceNotFoundException;
import com.sky.usermanager.model.User;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectFactory;
import com.sky.usermanager.repository.UserExternalProjectRepository;
import com.sky.usermanager.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExternalProjectService {

    private static final Logger log = LoggerFactory.getLogger(ExternalProjectService.class);
    public static final String PROJECTS_CREATED_TOTAL_COUNTER = "projects_created_total";

    private final UserRepository userRepository;
    private final UserExternalProjectRepository userExternalProjectRepository;
    private final Counter projectsCreatedCounter;
    private final UserExternalProjectFactory userExternalProjectFactory;

    public ExternalProjectService(UserRepository userRepository, UserExternalProjectRepository userExternalProjectRepository,
                                  MeterRegistry registry, UserExternalProjectFactory userExternalProjectFactory) {
        this.userRepository = userRepository;
        this.userExternalProjectRepository = userExternalProjectRepository;
        this.projectsCreatedCounter = Counter.builder(PROJECTS_CREATED_TOTAL_COUNTER)
                .description("Total number of external projects created")
                .register(registry);
        this.userExternalProjectFactory = userExternalProjectFactory;
    }

    @Transactional
    public UserExternalProject addExternalProject(Long userId, UserExternalProjectDTO projectDTO) {
        log.info("Starting to add external project [userId={}, projectId={}, name={}]",
                userId, projectDTO.id(), projectDTO.name());
        try {
            validateProjectId(userId, projectDTO.id());
            validateProjectName(userId, projectDTO);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found when adding project [userId={}, projectId={}]",
                                userId, projectDTO.id());
                        return new ResourceNotFoundException("User not found with id " + userId);
                    });

            if (user.getExternalProjects().stream().anyMatch(p -> p.getId().equals(projectDTO.id()))) {
                log.warn("Duplicate project link detected [userId={}, projectId={}]",
                        userId, projectDTO.id());
                throw new IllegalArgumentException(String.format("Project with id '%s' is already linked to user with id '%d'", projectDTO.id(), userId));
            }

            UserExternalProject userExternalProject = userExternalProjectFactory.create(user, projectDTO.id(), projectDTO.name());
            log.info("External project linked successfully [userId={}, projectId={}, name={}]",
                    userId, userExternalProject.getId(), userExternalProject.getName());
            projectsCreatedCounter.increment();
            return userExternalProject;
        } catch (Exception e) {
            log.error("Error adding external project [userId={}, projectId={}, name={}]: {}",
                    userId, projectDTO.id(), projectDTO.name(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<UserExternalProject> getUserProjects(Long userId) {
        log.debug("Fetching external projects for user [userId={}]", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found when fetching user projects [userId={}]", userId);
                        return new ResourceNotFoundException("User not found with id " + userId);
                    });

            List<UserExternalProject> externalProjects = user.getExternalProjects();
            log.info("Retrieved {} external projects for user [userId={}]",
                    externalProjects.size(), userId);

            return externalProjects;
        } catch (Exception e) {
            log.error("Error fetching external projects for user [userId={}]: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserExternalProject getUserProjectById(Long userId, String projectId) {
        log.info("Fetching external project [userId={}, projectId={}]", userId, projectId);

        try {
            validateProjectId(userId, projectId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found when fetching project for user [userId={}]", userId);
                        return new ResourceNotFoundException("User not found with id " + userId);
                    });

            return user.getExternalProjects().stream().filter(p -> p.getId().equals(projectId))
                    .findFirst()
                    .map(p -> {
                        log.info("Project found [userId={}, projectId={}, name={}]",
                                userId, p.getId(), p.getName());
                        return p;
                    })
                    .orElseThrow(() -> {
                        log.warn("Project not found for user [userId={}, projectId={}]", userId, projectId);
                        return new ResourceNotFoundException(
                                String.format("Project with id '%s' not found for user with id '%d'", projectId, userId));
                    });
        } catch (Exception e) {
            log.error("Error fetching external project [userId={}, projectId={}]: {}", userId, projectId, e.getMessage(), e);
            throw e;
        }

    }

    private void validateProjectId(Long userId, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            log.warn("Project creation failed - project id is blank [userId={}]", userId);
            throw new IllegalArgumentException("Project id must not be blank");
        }
    }

    private void validateProjectName(Long userId, UserExternalProjectDTO projectDTO) {
        if (projectDTO.name() == null || projectDTO.name().isBlank()) {
            log.warn("Project creation failed - project name is blank [userId={}, projectId={}]",
                    userId, projectDTO.id());
            throw new IllegalArgumentException("Project name must not be blank");
        }
    }
}
