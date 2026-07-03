package com.project.repositories;

import com.project.models.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Integer> {
    boolean existsByUsername(String username);
    Optional<Security> findByUsername(String username);
}
