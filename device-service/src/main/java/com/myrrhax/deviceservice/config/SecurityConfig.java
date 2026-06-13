package com.myrrhax.deviceservice.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .cors(CorsConfigurer::disable)
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, "/api/v1/device").hasAuthority("SCOPE_device:modify")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/device/**").hasAuthority("SCOPE_device:modify")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/device/**").hasAuthority("SCOPE_device:modify")
                        .requestMatchers(HttpMethod.GET, "/api/v1/device/**").hasAuthority("SCOPE_device:read")
                        .anyRequest().authenticated())
                .build();
    }
}
