package com.myrrhax.ingestionservice.service;

import com.myrrhax.ingestionservice.dto.EnergyUsageDto;
import com.myrrhax.ingestionservice.event.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {
    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    @SneakyThrows
    public void ingestEnergyUsage(EnergyUsageDto dto) {
        EnergyUsageEvent event = new EnergyUsageEvent(
                dto.deviceId(),
                dto.energyConsumed(),
                dto.timestamp()
        );

        kafkaTemplate.send("energy-usage", event).get();
        log.info("Energy usage event {} sent to topic", dto.deviceId());
    }
}
