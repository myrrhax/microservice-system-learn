package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceClient {
    private final RestTemplate restTemplate;

    @Value("${app.client.device-url}")
    private String baseUrl;

    private static final ParameterizedTypeReference<List<DeviceDto>> LIST_OF_DEVICES =
            new ParameterizedTypeReference<>() {};

    @Async
    public CompletableFuture<Optional<DeviceDto>> getDeviceById(Long deviceId) {
        log.info("Fetching device by id {}", deviceId);
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(Map.of("deviceId", deviceId))
                .toUriString();

        ResponseEntity<DeviceDto> response = restTemplate.getForEntity(uri, DeviceDto.class);

        return CompletableFuture.completedFuture(
                Optional.ofNullable(response.getBody())
        );
    }

    public List<DeviceDto> getAllDevicesForUser(Long userId) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<List<DeviceDto>> response = restTemplate.exchange(uri,
                HttpMethod.GET,
                null,
                LIST_OF_DEVICES);

        return Objects.requireNonNullElse(response.getBody(), Collections.emptyList());
    }
}
