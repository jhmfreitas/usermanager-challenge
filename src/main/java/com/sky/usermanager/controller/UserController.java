package com.sky.usermanager.controller;

import com.sky.usermanager.controller.dto.UserDTO;
import com.sky.usermanager.controller.dto.UserResponseDTO;
import com.sky.usermanager.model.User;
import com.sky.usermanager.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Added for RBAC demonstration purposes only
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        log.info("Received request to list users");
        List<UserResponseDTO> userResponseDTOList = userService.getUsers().stream().map(UserResponseDTO::from).toList();
        log.info("Returning {} users", userResponseDTOList.size());
        return ResponseEntity.ok(userResponseDTOList);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserDTO user, UriComponentsBuilder uriBuilder) {
        log.info("Received request to create user [email={}]", user.email());
        User created = userService.createUser(user);

        URI location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        UserResponseDTO userResponseDTO = UserResponseDTO.from(created);

        log.info("User successfully created [userId={}, location={}]", created.getId(), location);

        return ResponseEntity
                .created(location)
                .body(userResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by ID [userId={}]", id);
        User user = userService.getUserById(id);
        UserResponseDTO userResponseDTO = UserResponseDTO.from(user);
        log.info("Returning user [userId={}]", user.getId());
        return ResponseEntity.ok(userResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user [userId={}]", id);
        userService.deleteUserById(id);
        log.info("User deleted successfully [userId={}]", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        log.info("Received request to update user [userId={}, email={}]", id, userDTO.email());
        User user = userService.updateUser(id, userDTO);
        UserResponseDTO userResponseDTO = UserResponseDTO.from(user);
        log.info("User updated successfully [userId={}, email={}]", user.getId(), user.getEmail());
        return ResponseEntity.ok(userResponseDTO);
    }
}
