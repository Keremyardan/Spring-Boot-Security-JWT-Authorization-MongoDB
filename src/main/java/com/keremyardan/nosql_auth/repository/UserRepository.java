package com.keremyardan.nosql_auth.repository;

import com.keremyardan.nosql_auth.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional <User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
