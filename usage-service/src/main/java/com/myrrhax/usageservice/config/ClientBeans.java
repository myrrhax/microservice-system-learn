package com.myrrhax.usageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientBeans {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
