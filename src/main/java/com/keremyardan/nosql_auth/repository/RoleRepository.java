package com.keremyardan.nosql_auth.repository;

import com.keremyardan.nosql_auth.Entity.ERole;
import com.keremyardan.nosql_auth.Entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository <Role, String>{
    Optional<Role> findByName(ERole name);
}
