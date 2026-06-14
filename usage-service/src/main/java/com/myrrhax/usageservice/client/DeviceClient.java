package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class DeviceClient {
    private final RestTemplate restTemplate;

    private static final ParameterizedTypeReference<List<DeviceDto>> LIST_OF_DEVICES =
            new ParameterizedTypeReference<>() {};

    @Async
    public CompletableFuture<Optional<DeviceDto>> getDeviceById(Long deviceId) {
        log.info("Fetching device by id {}", deviceId);
        ResponseEntity<DeviceDto> response = restTemplate.getForEntity("/" + deviceId, DeviceDto.class);

        return CompletableFuture.completedFuture(
                Optional.ofNullable(response.getBody())
        );
    }

    public List<DeviceDto> getAllDevicesForUser(Long userId) {
        ResponseEntity<List<DeviceDto>> response = restTemplate.exchange("/user/" + userId,
                HttpMethod.GET,
                null,
                LIST_OF_DEVICES);

        return Objects.requireNonNullElse(response.getBody(), Collections.emptyList());
    }
}
