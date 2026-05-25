package com.myrrhax.userservice.service;

import com.myrrhax.userservice.entity.User;
import com.myrrhax.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@Transactional
@Profile("dev")
@RequiredArgsConstructor
public class DevDataFillerService implements ApplicationRunner {
    private static final int NUMBER_OF_USERS = 10;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Populating user data...");
        if (userRepository.count() > 0) {
            log.info("Database already has device information");
            return;
        }

        List<User> users = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            var user = User.builder()
                    .name("User" + i)
                    .surname("Surname" + i)
                    .email("user" + i + "@example.com")
                    .address(i + " Example St.")
                    .alerting(i % 2 == 0)
                    .energyAlertingThreshold(1000.0 + i)
                    .build();

            users.add(user);
        }

        userRepository.saveAll(users);
        log.info("User repository has been populated with {} users", NUMBER_OF_USERS);
    }
}
