package com.in.rohit.spring_ai_agent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.in.rohit.spring_ai_agent.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}