package com.myrrhax.deviceservice.security;

import com.myrrhax.deviceservice.client.UserClient;
import com.myrrhax.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeviceAccessManager {
    private final UserClient userClient;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public boolean hasAccessToDevice(Authentication authentication, Long deviceId) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return false;
        }

        String subject = jwtAuthenticationToken.getToken().getSubject();

        return userClient.getUserBySubId(subject)
                .map(user -> deviceRepository.existsByIdAndUserId(deviceId, user.id()))
                .orElse(false);
    }
}
