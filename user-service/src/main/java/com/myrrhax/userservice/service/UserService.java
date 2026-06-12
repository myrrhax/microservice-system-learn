package com.myrrhax.userservice.service;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.CreateUserDto;
import com.myrrhax.userservice.dto.request.UpdateUserRequest;
import com.myrrhax.userservice.entity.User;
import com.myrrhax.userservice.exception.ApplicationException;
import com.myrrhax.userservice.exception.UserNotFoundException;
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
    public UserDto createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("User with email {} already exists", dto.email());

            throw new ApplicationException("User with email " + dto.email() + " already exists");
        }

        User user = userMapper.toEntity(dto);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return userMapper.toDto(getUserEntity(id));
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest dto) {
        User user = getUserEntity(id);

        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setEmail(dto.email());
        user.setAddress(dto.address());
        user.setAlerting(dto.alerting());
        user.setEnergyAlertingThreshold(dto.energyAlertingThreshold());

        return userMapper.toDto(userRepository.save(user));
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
