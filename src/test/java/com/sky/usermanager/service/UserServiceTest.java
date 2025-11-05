package com.sky.usermanager.service;

import com.sky.usermanager.controller.dto.UserDTO;
import com.sky.usermanager.exception.DuplicateResourceException;
import com.sky.usermanager.exception.ResourceNotFoundException;
import com.sky.usermanager.model.User;
import com.sky.usermanager.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.sky.usermanager.TestUtil.JOHN_DOE_NAME;
import static com.sky.usermanager.TestUtil.JOHN_EXAMPLE_EMAIL;
import static com.sky.usermanager.service.UserService.USERS_CREATED_TOTAL_COUNTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    public static final String NEW_EXAMPLE_EMAIL = "new@example.com";
    public static final String NEWPASS = "newpass";
    public static final String HASHED_NEW_PASS = "hashedNewPass";
    public static final String NEW_NAME = "New Name";
    public static final String ENCODED_PASS = "encodedPass";
    public static final String PASSWORD_123 = "password123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SimpleMeterRegistry meterRegistry;

    private UserService userService;

    private UserDTO dto;
    private User existingUser;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userService = new UserService(userRepository, passwordEncoder, meterRegistry);
        dto = new UserDTO(JOHN_EXAMPLE_EMAIL, PASSWORD_123, JOHN_DOE_NAME);
        existingUser = new User(JOHN_EXAMPLE_EMAIL, "hashedpass", JOHN_DOE_NAME);
    }

    @Test
    @DisplayName("createUser should hash password and save new user")
    void createUser_ShouldSaveUser_WhenEmailNotExists() {
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn(ENCODED_PASS);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(dto);

        assertThat(result.getEmail()).isEqualTo(dto.email());
        assertThat(result.getPassword()).isEqualTo(ENCODED_PASS);
        assertThat(result.getName()).isEqualTo(dto.name());

        verify(passwordEncoder).encode(PASSWORD_123);
        verify(userRepository).save(any(User.class));

        double metricValue = meterRegistry.get(USERS_CREATED_TOTAL_COUNTER).counter().count();
        assertEquals(1.0, metricValue, "The metric counter should have been incremented once");
    }

    @Test
    @DisplayName("createUser should throw when email already exists")
    void createUser_ShouldThrow_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("The email address provided is already in use");

        verify(userRepository, never()).save(any());
    }

    static Stream<Arguments> invalidUserInputs() {
        return Stream.of(
                Arguments.of(null, PASSWORD_123, IllegalArgumentException.class, "null email"),
                Arguments.of("", PASSWORD_123, IllegalArgumentException.class, "blank email"),
                Arguments.of("user@example.com", null, IllegalArgumentException.class, "null password"),
                Arguments.of("user@example.com", "", IllegalArgumentException.class, "blank password"),
                Arguments.of("duplicate@example.com", PASSWORD_123, DuplicateResourceException.class, "duplicate email")
        );
    }

    @ParameterizedTest(name = "[{index}] should fail when {3}")
    @MethodSource("invalidUserInputs")
    @DisplayName("createUser should throw appropriate exceptions for invalid inputs")
    void createUser_ShouldThrowExceptions_WhenInvalidUserInputs(
            String email,
            String password,
            Class<? extends Exception> expectedException,
            String reason
    ) {
        if ("duplicate email".equals(reason)) {
            when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);
        }

        UserDTO dto = new UserDTO(email, password, JOHN_DOE_NAME);

        assertThrows(expectedException, () -> userService.createUser(dto));
    }

    @Test
    @DisplayName("getUsers should return all users")
    void getUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<User> users = userService.getUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo(JOHN_EXAMPLE_EMAIL);
    }

    @Test
    @DisplayName("getUserById should return user when exists")
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(existingUser);
    }

    @Test
    @DisplayName("getUserById should throw when not found")
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("deleteUserById should delete when exists")
    void deleteUserById_ShouldDelete_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUserById should throw when user not found")
    void deleteUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateUser should update and re-hash password")
    void updateUser_ShouldUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(NEWPASS)).thenReturn(HASHED_NEW_PASS);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO updateDto = new UserDTO(NEW_EXAMPLE_EMAIL, NEWPASS, NEW_NAME);
        User updated = userService.updateUser(1L, updateDto);

        assertThat(updated.getEmail()).isEqualTo(NEW_EXAMPLE_EMAIL);
        assertThat(updated.getPassword()).isEqualTo(HASHED_NEW_PASS);
        assertThat(updated.getName()).isEqualTo(NEW_NAME);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw when user not found")
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(42L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}
