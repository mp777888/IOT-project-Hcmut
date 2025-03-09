package com.example.iot_project.Repository;

import com.example.iot_project.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
