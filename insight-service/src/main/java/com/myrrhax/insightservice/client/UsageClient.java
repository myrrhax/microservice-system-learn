package com.myrrhax.insightservice.client;

import com.myrrhax.insightservice.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageClient {
    private final RestTemplate restTemplate;

    @Value("${app.client.usage-url}")
    private String baseUrl;


    public Optional<UsageDto> getXDaysUsageForUser(Long userId, int overviewDays) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", overviewDays)
                .buildAndExpand(userId)
                .toUriString();

        try {
            ResponseEntity<UsageDto> response = restTemplate.getForEntity(uri, UsageDto.class);

            return Optional.ofNullable(response.getBody());
        } catch (RestClientException e) {
            log.error("Failed to get user {} usage from usage-service", userId, e);

            return Optional.empty();
        }
    }
}
