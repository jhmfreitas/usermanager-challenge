package com.sky.usermanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.usermanager.controller.dto.UserExternalProjectDTO;
import com.sky.usermanager.model.User;
import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectFactory;
import com.sky.usermanager.repository.UserExternalProjectRepository;
import com.sky.usermanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.sky.usermanager.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserExternalProjectControllerIntegrationTest {

    public static final String PROJ_101 = "PROJ-101";
    public static final String PROJ_202 = "PROJ-202";
    public static final String SKY_PLATFORM_MIGRATION = "Sky Platform Migration";
    public static final String PROJ_001 = "PROJ-001";
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserExternalProjectFactory userExternalProjectFactory;

    @Autowired
    private UserExternalProjectRepository projectRepository;

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should add a new external project to a user")
    void shouldAddExternalProjectToUser() throws Exception {
        User user = new User(JOHN_EXAMPLE_EMAIL, "hashed_password", JOHN_DOE_NAME);
        userRepository.save(user);

        UserExternalProjectDTO projectDTO = new UserExternalProjectDTO(PROJ_001, SKY_PLATFORM_MIGRATION);

        mockMvc.perform(post("/api/users/{userId}/projects", user.getId())
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(PROJ_001))
                .andExpect(jsonPath("$.projectName").value(SKY_PLATFORM_MIGRATION))
                .andReturn()
                .getResponse();

        List<UserExternalProject> projects = projectRepository.findAll();
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should retrieve all external projects for a user")
    void shouldRetrieveExternalProjectsFromUser() throws Exception {
        User user = new User("john.doe2@example.com", "hashed_password", "John Doe 2");
        userRepository.save(user);

        UserExternalProject project1 = userExternalProjectFactory.create(user, PROJ_101, SKY_PLATFORM_MIGRATION);
        UserExternalProject project2 = userExternalProjectFactory.create(user, PROJ_202, "Sky Platform Refactor");

        projectRepository.saveAll(List.of(project1, project2));

        mockMvc.perform(get("/api/users/{userId}/projects", user.getId())
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(PROJ_101))
                .andExpect(jsonPath("$[1].id").value(PROJ_202));
    }
}
