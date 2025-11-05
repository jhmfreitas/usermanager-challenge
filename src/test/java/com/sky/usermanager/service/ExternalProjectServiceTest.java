package com.sky.usermanager.service;

import com.sky.usermanager.controller.dto.UserExternalProjectDTO;
import com.sky.usermanager.exception.ResourceNotFoundException;
import com.sky.usermanager.model.User;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectFactory;
import com.sky.usermanager.repository.UserExternalProjectRepository;
import com.sky.usermanager.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sky.usermanager.service.ExternalProjectService.PROJECTS_CREATED_TOTAL_COUNTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalProjectServiceTest {

    private ExternalProjectService externalProjectService;

    @Mock
    private UserRepository userRepository;

    private UserExternalProjectFactory userExternalProjectFactory;

    @Mock
    private UserExternalProjectRepository userExternalProjectRepository;

    private SimpleMeterRegistry meterRegistry;

    private final Long USER_ID = 101L;
    private User mockUser;
    private UserExternalProjectDTO projectDTO;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userExternalProjectFactory = new UserExternalProjectFactory();
        externalProjectService =
                new ExternalProjectService(userRepository, userExternalProjectRepository, meterRegistry,
                        userExternalProjectFactory);

        mockUser = mock(User.class);

        projectDTO = new UserExternalProjectDTO("proj-123", "Project Alpha");
    }

    @Test
    @DisplayName("Should successfully add a new external project and increment the metric counter")
    void addExternalProject_ShouldCreateProjectAndIncrementCounter() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(mockUser.getExternalProjects()).thenReturn(Collections.emptyList());

        UserExternalProjectFactory spyFactory = spy(userExternalProjectFactory);
        externalProjectService =
                new ExternalProjectService(userRepository, userExternalProjectRepository, meterRegistry, spyFactory);

        UserExternalProject createdProject = externalProjectService.addExternalProject(USER_ID, projectDTO);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(spyFactory, times(1)).create(mockUser, projectDTO.id(), projectDTO.name());

        assertNotNull(createdProject, "The created project should not be null");
        assertEquals(projectDTO.id(), createdProject.getId(), "Project ID should match DTO");
        assertEquals(projectDTO.name(), createdProject.getName(), "Project name should match DTO");

        double metricValue = meterRegistry.get(PROJECTS_CREATED_TOTAL_COUNTER).counter().count();
        assertEquals(1.0, metricValue, "The metric counter should have been incremented once");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when adding project for non-existent user")
    void addExternalProject_UserNotFound_ThrowsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> externalProjectService.addExternalProject(USER_ID, projectDTO),
                        "Expected ResourceNotFoundException to be thrown, but it wasn't.");

        assertTrue(thrown.getMessage().contains("User not found with id " + USER_ID));

        verify(userExternalProjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully retrieve all external projects for a user")
    void getUserProjects_ShouldReturnProjectsForUser() {
        UserExternalProject projectA = userExternalProjectFactory.create(mockUser, "pA", "Project A");
        UserExternalProject projectB = userExternalProjectFactory.create(mockUser, "pB", "Project B");
        List<UserExternalProject> expectedProjects = List.of(projectA, projectB);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(mockUser.getExternalProjects()).thenReturn(expectedProjects);

        List<UserExternalProject> actualProjects = externalProjectService.getUserProjects(USER_ID);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(mockUser, times(1)).getExternalProjects();

        assertNotNull(actualProjects, "The returned list of projects should not be null.");
        assertEquals(
                2, actualProjects.size(), "The size of the projects list should match the mocked size.");
        assertSame(
                expectedProjects,
                actualProjects,
                "The service should return the exact list from the User entity (no defensive copy needed in this simple case).");
    }

    @Test
    @DisplayName("Should return an empty list if the user has no external projects")
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjects() {
        List<UserExternalProject> emptyList = Collections.emptyList();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(mockUser.getExternalProjects()).thenReturn(emptyList);

        List<UserExternalProject> actualProjects = externalProjectService.getUserProjects(USER_ID);

        verify(userRepository, times(1)).findById(USER_ID);

        assertNotNull(actualProjects);
        assertTrue(actualProjects.isEmpty());
        assertSame(
                emptyList,
                actualProjects,
                "The returned list should be the exact empty list from the User entity.");
    }

    @Test
    @DisplayName(
            "Should throw ResourceNotFoundException when retrieving projects for non-existent user")
    void getUserProjects_ShouldThrowsException_WhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> externalProjectService.getUserProjects(USER_ID),
                "Expected ResourceNotFoundException to be thrown, but it wasn't.");

        verify(mockUser, never()).getExternalProjects();
    }
}
