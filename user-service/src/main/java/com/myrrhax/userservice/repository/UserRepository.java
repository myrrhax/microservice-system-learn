package com.myrrhax.userservice.repository;

import com.myrrhax.userservice.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findBySubId(String subId);
}
