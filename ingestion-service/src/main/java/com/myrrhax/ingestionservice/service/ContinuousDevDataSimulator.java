package com.myrrhax.ingestionservice.service;

import com.myrrhax.ingestionservice.dto.EnergyUsageDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@Profile("dev")
public class ContinuousDevDataSimulator {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    @Value("${app.simulation.requests-per-interval}")
    private int requestPerInterval;

    @Value("${server.port}")
    private int port;

    private String url;

    @PostConstruct
    public void init() {
        this.url = "http://localhost:" + port + "/api/v1/ingestion";
    }

    @PreDestroy
    public void destroy() {
        this.executor.shutdown();
    }

    @Scheduled(fixedRateString = "${app.simulation.interval-ms}")
    public void simulateData() {
        log.info("Data simulation started");
        for (int i = 0; i < requestPerInterval; i++) {
            EnergyUsageDto dto = new EnergyUsageDto(
                    random.nextLong(1,6),
                    Math.round(random.nextDouble(0.0, 10.0) * 100.0) / 100.0,
                    Instant.now()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
            executor.execute(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<EnergyUsageDto> entity = new HttpEntity<>(dto, headers);

                    restTemplate.postForEntity(this.url, entity, Void.class);
                    log.info("Data simulation was sent");
                } catch (Exception e) {
                    log.error("Failed to simulate data", e);
                }
            });
        }
    }
}
