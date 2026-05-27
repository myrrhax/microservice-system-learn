package com.myrrhax.usageservice.client;

import com.myrrhax.usageservice.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceClient {
    private final RestTemplate restTemplate;

    @Value("app.client.device-url")
    private String baseUrl;

    public Optional<DeviceDto> getDeviceById(Long deviceId) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(Map.of("deviceId", deviceId))
                .toUriString();

        ResponseEntity<DeviceDto> response = restTemplate.getForEntity(uri, DeviceDto.class);

        return Optional.ofNullable(response.getBody());
    }
}
