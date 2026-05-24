package com.myrrhax.userservice.service;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.request.CreateUserRequest;
import com.myrrhax.userservice.entity.User;
import com.myrrhax.userservice.exception.ApplicationException;
import com.myrrhax.userservice.mapper.UserMapper;
import com.myrrhax.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(CreateUserRequest dto) {
        log.info("Creating user with email {}", dto.email());

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("User with email {} already exists", dto.email());

            throw new ApplicationException("User with email " + dto.email() + " already exists");
        }

        User user = userMapper.toEntity(dto);
        userRepository.save(user);

        return userMapper.toDto(user);
    }
}
