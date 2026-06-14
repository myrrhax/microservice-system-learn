package com.myrrhax.deviceservice.client;

import com.myrrhax.deviceservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class RestTemplateUserClient implements UserClient {
    private final RestTemplate userRestTemplate;

    public RestTemplateUserClient(@Qualifier("userRestTemplate") RestTemplate userRestTemplate) {
        this.userRestTemplate = userRestTemplate;
    }

    @Override
    public Optional<UserDto> getUserBySubId(String subId) {
        try {
            ResponseEntity<UserDto> user = userRestTemplate.getForEntity("/sub/" + subId, UserDto.class);
            if (user.getStatusCode().is2xxSuccessful() && user.hasBody()) {
                return Optional.of(Objects.requireNonNull(user.getBody()));
            }

            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch user with sub id {}", subId, e);

            return Optional.empty();
        }
    }
}
