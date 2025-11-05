package com.sky.usermanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.usermanager.controller.dto.UserDTO;
import com.sky.usermanager.model.User;
import com.sky.usermanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.sky.usermanager.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository repo;

    private UserDTO dto;

    @BeforeEach
    void setUp() {
        dto = new UserDTO("john2@example.com", "pwd", JOHN_DOE_NAME);
    }

    @Test
    @DisplayName("Should create a new user and return 201 with Location header")
    void shouldCreateUser_WhenAdminCredentialsAndValidData() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Should not create a new user when email is duplicated")
    void shouldNotCreateUser_WhenEmailIsDuplicated() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should retrieve users by id")
    void shouldRetrieveUserById() throws Exception {
        User saved = repo.save(new User(JOHN_EXAMPLE_EMAIL, "hashed", "John"));

        mockMvc.perform(get("/api/users/{id}", saved.getId())
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(JOHN_EXAMPLE_EMAIL));
    }

    @Test
    @DisplayName("Should delete user by id")
    void shouldDeleteUser() throws Exception {
        User saved = repo.save(new User("to.delete@example.com", "hashed", "Temp"));

        mockMvc.perform(delete("/api/users/{id}", saved.getId())
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        assertThat(repo.existsById(saved.getId())).isFalse();
    }
}
