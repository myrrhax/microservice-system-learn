package com.myrrhax.userservice.service;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.request.CreateUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    public UserDto createUser(CreateUserRequest dto) {
        log.info("Creating user: {}", dto);

        return null;
    }
}
