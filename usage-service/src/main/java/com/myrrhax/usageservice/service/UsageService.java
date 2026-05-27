package com.myrrhax.usageservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.myrrhax.usageservice.config.properties.InfluxConnection;
import com.myrrhax.usageservice.event.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(topics = "energy-usage", containerFactory = "kafkaListenerContainerFactory")
@RequiredArgsConstructor
public class UsageService {
    private final InfluxDBClient influx;
    private final InfluxConnection influxConn;

    @KafkaHandler
    public void handleEnergyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        log.info("New energy usage event received: {}", energyUsageEvent);

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);
        influx.getWriteApiBlocking().writePoint(influxConn.bucket(), influxConn.org(), point);
        log.info("New energy usage event sent to influx for device: {}", energyUsageEvent.deviceId());
    }
}
