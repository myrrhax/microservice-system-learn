package com.myrrhax.deviceservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {
    @Bean
    public RestTemplate userRestTemplate(
            @Value("${app.clients.user.base-url}") String userClientBaseUrl,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        var clientInterceptor = new OAuth2ClientHttpRequestInterceptor(
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientRepository
                )
        );
        clientInterceptor.setClientRegistrationIdResolver(request -> "keycloak");

        return new RestTemplateBuilder()
                .rootUri(userClientBaseUrl)
                .interceptors(clientInterceptor)
                .build();
    }
}
