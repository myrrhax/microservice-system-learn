package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class DeviceClient {
    private final RestTemplate restTemplate;

    @Value("${app.client.device-url}")
    private String baseUrl;

    @Async
    public CompletableFuture<Optional<DeviceDto>> getDeviceById(Long deviceId) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(Map.of("deviceId", deviceId))
                .toUriString();

        ResponseEntity<DeviceDto> response = restTemplate.getForEntity(uri, DeviceDto.class);

        return CompletableFuture.completedFuture(
                Optional.ofNullable(response.getBody())
        );
    }
}
