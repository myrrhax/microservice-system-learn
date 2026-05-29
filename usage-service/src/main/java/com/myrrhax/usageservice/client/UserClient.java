package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplate restTemplate;

    @Value("${app.client.user-url}")
    private String baseUrl;

    @Async
    public CompletableFuture<Optional<UserDto>> getUserById(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);

        return CompletableFuture.completedFuture(
                Optional.ofNullable(response.getBody())
        );
    }
}
