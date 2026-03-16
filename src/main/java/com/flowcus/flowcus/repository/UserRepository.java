package com.flowcus.flowcus.repository;

import com.flowcus.flowcus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    // Used for login and unique checks during registration
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    // used for search functionality to find people/user in people page
    List<User> findByUsernameStartingWithIgnoreCase(String username);

}
