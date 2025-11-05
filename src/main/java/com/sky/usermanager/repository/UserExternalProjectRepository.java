package com.sky.usermanager.repository;

import com.sky.usermanager.model.UserExternalProject;
import com.sky.usermanager.model.UserExternalProjectKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExternalProjectRepository extends
    JpaRepository<UserExternalProject, UserExternalProjectKey> {

  Page<UserExternalProject> findByUserId(Long userId, Pageable pageable);

}
