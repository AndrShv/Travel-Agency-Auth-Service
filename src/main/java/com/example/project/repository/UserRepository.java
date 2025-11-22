package com.example.project.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    Optional<User> findUserByUsername(String username);


    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    List<User> findByUsernameContainingIgnoreCase(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);


}
