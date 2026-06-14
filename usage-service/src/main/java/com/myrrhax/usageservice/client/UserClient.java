package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplate restTemplate;

    @Async
    public CompletableFuture<Optional<UserDto>> getUserById(Long userId) {
        log.info("Fetching user by id {}", userId);
        ResponseEntity<UserDto> response = restTemplate.getForEntity("/" + userId, UserDto.class);

        return CompletableFuture.completedFuture(
                Optional.ofNullable(response.getBody())
        );
    }
}
