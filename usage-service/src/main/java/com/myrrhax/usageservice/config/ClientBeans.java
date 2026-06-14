package com.myrrhax.usageservice.config;

import com.myrrhax.usageservice.client.DeviceClient;
import com.myrrhax.usageservice.client.UserClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientBeans {
    @Bean
    public UserClient userClient(@Value("${app.client.user-url}") String userUrl,
                                 OAuth2ClientHttpRequestInterceptor keycloakClientInterceptor) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .interceptors(keycloakClientInterceptor)
                .rootUri(userUrl)
                .build();

        return new UserClient(restTemplate);
    }

    @Bean
    public DeviceClient deviceClient(@Value("${app.client.device-url}") String deviceUrl,
                                     OAuth2ClientHttpRequestInterceptor keycloakClientInterceptor) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .interceptors(keycloakClientInterceptor)
                .rootUri(deviceUrl)
                .build();

        return new DeviceClient(restTemplate);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    @Bean
    public OAuth2ClientHttpRequestInterceptor keycloakClientCredentialsInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        interceptor.setClientRegistrationIdResolver(request -> "keycloak");

        interceptor.setPrincipalResolver(request ->
                new UsernamePasswordAuthenticationToken(
                        "usage-service",
                        "N/A",
                        AuthorityUtils.NO_AUTHORITIES
                )
        );

        return interceptor;
    }
}