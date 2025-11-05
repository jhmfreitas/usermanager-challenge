package com.sky.usermanager.service;

import com.sky.usermanager.controller.dto.UserDTO;
import com.sky.usermanager.exception.DuplicateResourceException;
import com.sky.usermanager.exception.ResourceNotFoundException;
import com.sky.usermanager.model.User;
import com.sky.usermanager.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public static final String USERS_CREATED_TOTAL_COUNTER = "users_created_total";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Counter userCreatedCounter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MeterRegistry registry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCreatedCounter = Counter.builder(USERS_CREATED_TOTAL_COUNTER)
                .description("Total number of users created")
                .register(registry);
    }

    public String hashPassword(String rawPassword) {
        return this.passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public User createUser(UserDTO userDTO) {
        String providedEmail = userDTO.email();
        String providedPassword = userDTO.password();
        log.info("Starting user creation [email={}]", providedEmail);

        try {
            if (providedEmail == null || providedEmail.isBlank()) {
                log.warn("User creation failed — email is blank");
                throw new IllegalArgumentException("Email must not be blank");
            }

            if (this.userRepository.existsByEmail(providedEmail)) {
                log.warn("Duplicate email detected during user creation [email={}]", providedEmail);
                throw new DuplicateResourceException("The email address provided is already in use: " + providedEmail);
            }

            if (providedPassword == null || providedPassword.isBlank()) {
                log.warn("User creation failed — password is blank [email={}]", providedEmail);
                throw new IllegalArgumentException("Password must not be blank");
            }

            User user = new User(providedEmail, hashPassword(providedPassword),
                    userDTO.name());
            User savedUser = this.userRepository.save(user);
            userCreatedCounter.increment();

            log.info("User created successfully [id={}, email={}]", savedUser.getId(), savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating user [email={}]: {}", providedEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        log.debug("Fetching all users from database");
        List<User> users = this.userRepository.findAll();
        log.info("Retrieved {} users ", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.debug("Fetching user by ID [userId={}]", id);
        return this.userRepository.findById(id)
                .map(user -> {
                    log.info("User found [userId={}]", user.getId());
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("User not found [userId={}]", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("Attempting to delete user [userId={}]", id);

        try {
            if (!this.userRepository.existsById(id)) {
                log.warn("Delete failed — user not found [userId={}]", id);
                throw new ResourceNotFoundException("User not found with id " + id);
            }

            this.userRepository.deleteById(id);
            log.info("User deleted successfully [userId={}]", id);
        } catch (Exception e) {
            log.error("Error deleting user [userId={}]: {}", id, e.getMessage(), e);
            throw e;
        }

    }

    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user [userId={}]", id);

        try {
            User user = this.userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Update failed — user not found [userId={}]", id);
                        return new ResourceNotFoundException("User not found with id " + id);
                    });

            String providedEmail = userDTO.email();
            String providedPassword = userDTO.password();

            if (providedEmail != null && !providedEmail.isBlank() && !providedEmail.equals(user.getEmail())) {
                if (this.userRepository.existsByEmail(providedEmail)) {
                    log.warn("Email already in use during update [newEmail={}]", providedEmail);
                    throw new IllegalArgumentException("Email already in use: " + providedEmail);
                }
                log.debug("Updating email [oldEmail={}, newEmail={}]", user.getEmail(), providedEmail);
                user.setEmail(providedEmail);
            }

            if (providedPassword != null && !providedPassword.isBlank()) {
                log.debug("Updating password for user [userId={}]", id);
                user.setPassword(hashPassword(providedPassword));
            }

            user.setName(userDTO.name());

            User updatedUser = this.userRepository.save(user);
            log.info("User updated successfully [userId={}, email={}]", updatedUser.getId(), updatedUser.getEmail());

            return updatedUser;
        } catch (Exception e) {
            log.error("Error updating user [userId={}]: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
