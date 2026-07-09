package com.project.repositories;

import com.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByPhone(String phone);
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE id = (SELECT user_id from user_security WHERE username=:username)")
    Optional<User> findByUsername(@Param("username") String username);
}
