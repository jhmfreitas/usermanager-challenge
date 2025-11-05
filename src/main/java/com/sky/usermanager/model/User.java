package com.sky.usermanager.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false, unique = true)
    private String email;

    @Column(length = 129, nullable = false)
    private String password;

    @Column(length = 120, nullable = true)
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserExternalProject> externalProjects = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
    }

    public User(String email, String password, String name) {
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email must not be null");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String hashedPassword) {
        this.password = Objects.requireNonNull(hashedPassword, "password must not be null");
    }

    public List<UserExternalProject> getExternalProjects() {
        return Collections.unmodifiableList(externalProjects);
    }

    void addExternalProject(UserExternalProject project) {
        Objects.requireNonNull(project, "project must not be null");
        project.setUser(this);
        externalProjects.add(project);
    }

    void removeExternalProject(UserExternalProject project) {
        Objects.requireNonNull(project, "project must not be null");
        project.setUser(null);
        externalProjects.remove(project);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(this.id, user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}
