package com.myrrhax.usageservice.config;

import com.myrrhax.usageservice.client.DeviceClient;
import com.myrrhax.usageservice.client.UserClient;
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
    public OAuth2ClientHttpRequestInterceptor keycloakClientCredentialsInterceptor(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientRepository
                )
        );
        interceptor.setClientRegistrationIdResolver(_ -> "keycloak");

        return interceptor;
    }
}