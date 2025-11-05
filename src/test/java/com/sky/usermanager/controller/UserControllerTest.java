package com.sky.usermanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.usermanager.config.SecurityConfig;
import com.sky.usermanager.controller.dto.UserDTO;
import com.sky.usermanager.exception.ResourceNotFoundException;
import com.sky.usermanager.model.User;
import com.sky.usermanager.service.UserService;
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

import java.util.List;

import static com.sky.usermanager.TestUtil.JOHN_DOE_NAME;
import static com.sky.usermanager.TestUtil.mockUser;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // Import to be consistent with CSRF disabling
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/users")
    class GetUsers {

        @Test
        @WithMockUser(
                username = "admin",
                roles = {"ADMIN"})
        @DisplayName("Should return list of users with status 200 with admin")
        void shouldReturnListOfUsers_WhenAdminCredentials() throws Exception {
            when(userService.getUsers()).thenReturn(List.of(mockUser()));

            mockMvc
                    .perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$[0].name").value(JOHN_DOE_NAME))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(userService).getUsers();
        }

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should not return list of users with status 403 for regular users")
        void shouldNotReturnListOfUsers_WhenUserCredentials() throws Exception {
            when(userService.getUsers()).thenReturn(List.of(mockUser()));

            mockMvc
                    .perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should return a user by ID")
        void shouldReturnUserById() throws Exception {
            when(userService.getUserById(1L)).thenReturn(mockUser());

            mockMvc
                    .perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.name").value(JOHN_DOE_NAME));
        }

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should return 404 if user not found")
        void shouldReturn404IfNotFound() throws Exception {
            when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

            mockMvc
                    .perform(get("/api/users/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should create a new user and return 201 with Location header")
        void shouldCreateUser() throws Exception {
            User user = mockUser();
            UserDTO request = new UserDTO("john.doe@example.com", "hashed_password", JOHN_DOE_NAME);

            when(userService.createUser(any(UserDTO.class))).thenReturn(user);

            mockMvc
                    .perform(
                            post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/users/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.name").value(JOHN_DOE_NAME));

            verify(userService).createUser(any(UserDTO.class));
        }

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should return 400 when email or password is missing")
        void shouldReturn400_WhenInvalidInput() throws Exception {
            UserDTO invalid = new UserDTO("", "", null);

            mockMvc
                    .perform(
                            post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("Should update an existing user and return 200")
        void shouldUpdateUser() throws Exception {
            User updated = mockUser();
            updated.setName("Updated Name");
            updated.setEmail("updated@example.com");

            UserDTO updateDTO = new UserDTO("updated@example.com", "hashed_password", "Updated Name");

            when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(updated);

            mockMvc
                    .perform(
                            put("/api/users/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("updated@example.com"))
                    .andExpect(jsonPath("$.name").value("Updated Name"));

            verify(userService).updateUser(eq(1L), any(UserDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @WithMockUser(
                username = "user",
                roles = {"USER"})
        @DisplayName("should delete user and return 204")
        void shouldDeleteUser() throws Exception {
            doNothing().when(userService).deleteUserById(1L);

            mockMvc.perform(delete("/api/users/1")).andExpect(status().isNoContent());

            verify(userService).deleteUserById(1L);
        }
    }
}
