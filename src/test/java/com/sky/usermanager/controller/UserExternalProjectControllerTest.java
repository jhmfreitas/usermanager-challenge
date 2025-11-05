package com.sky.usermanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.usermanager.config.SecurityConfig;
import com.sky.usermanager.controller.dto.UserExternalProjectDTO;
import com.sky.usermanager.model.User;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectFactory;
import com.sky.usermanager.service.ExternalProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.usermanager.TestUtil.mockUser;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserExternalProjectController.class)
@Import({SecurityConfig.class, UserExternalProjectFactory.class}) // Import SecurityConfig to be consistent with CSRF disabling
class UserExternalProjectControllerTest {

    public static final String PROJ_123 = "proj-123";
    public static final String TEST_PROJECT_NAME = "Test Project";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserExternalProjectFactory externalProjectFactory;

    @MockitoBean
    private ExternalProjectService externalProjectService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserExternalProject mockProject(User user) {
        UserExternalProject p = externalProjectFactory.create(user, PROJ_123, TEST_PROJECT_NAME);

        try {
            var created = UserExternalProject.class.getDeclaredField("createdAt");
            created.setAccessible(true);
            created.set(p, LocalDateTime.now());
        } catch (Exception ignored) {
        }
        return p;
    }


    @Nested
    @DisplayName("GET /api/users/{userId}/projects")
    class GetUserProjects {

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Should return all projects for a user")
        void shouldReturnAllProjectsForUser() throws Exception {
            User user = mockUser();
            UserExternalProject proj = mockProject(user);

            when(externalProjectService.getUserProjects(1L))
                    .thenReturn(List.of(proj));

            mockMvc.perform(get("/api/users/1/projects"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(PROJ_123))
                    .andExpect(jsonPath("$[0].projectName").value(TEST_PROJECT_NAME));

            verify(externalProjectService).getUserProjects(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/projects")
    class AddExternalProject {

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Should create project and return 201 with Location header")
        void shouldCreateExternalProject() throws Exception {
            User user = mockUser();
            UserExternalProject created = mockProject(user);

            UserExternalProjectDTO dto = new UserExternalProjectDTO(PROJ_123, TEST_PROJECT_NAME);

            when(externalProjectService.addExternalProject(eq(1L), any(UserExternalProjectDTO.class)))
                    .thenReturn(created);

            mockMvc.perform(post("/api/users/1/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/users/1/projects/proj-123")))
                    .andExpect(jsonPath("$.id").value(PROJ_123))
                    .andExpect(jsonPath("$.projectName").value(TEST_PROJECT_NAME));

            verify(externalProjectService).addExternalProject(eq(1L), any(UserExternalProjectDTO.class));
        }


        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Should return 400 if DTO validation fails")
        void shouldReturn400_WhenInvalidDTO() throws Exception {
            UserExternalProjectDTO invalid = new UserExternalProjectDTO("", "");

            mockMvc.perform(post("/api/users/1/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/projects/{projectId}")
    class GetUserProjectById {

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Should return a single project by ID")
        void shouldReturnProjectById() throws Exception {
            User user = mockUser();
            UserExternalProject project = mockProject(user);

            when(externalProjectService.getUserProjectById(1L, PROJ_123))
                    .thenReturn(project);

            mockMvc.perform(get("/api/users/1/projects/proj-123"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(PROJ_123))
                    .andExpect(jsonPath("$.projectName").value(TEST_PROJECT_NAME));

            verify(externalProjectService).getUserProjectById(1L, PROJ_123);
        }

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Should return 404 when project not found")
        void shouldReturn404_WhenProjectNotFound() throws Exception {
            when(externalProjectService.getUserProjectById(1L, "unknown"))
                    .thenThrow(new com.sky.usermanager.exception.ResourceNotFoundException("Not found"));

            mockMvc.perform(get("/api/users/1/projects/unknown"))
                    .andExpect(status().isNotFound());
        }
    }
}
