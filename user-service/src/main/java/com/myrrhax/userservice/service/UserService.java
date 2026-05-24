package com.myrrhax.userservice.service;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.request.CreateUserRequest;
import com.myrrhax.userservice.entity.User;
import com.myrrhax.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto createUser(CreateUserRequest dto) {
        log.info("Creating user with email {}", dto.email());

        User user = new User();

        return null;
    }
}
