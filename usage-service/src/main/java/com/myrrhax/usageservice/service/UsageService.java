package com.myrrhax.usageservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.myrrhax.usageservice.client.DeviceClient;
import com.myrrhax.usageservice.config.properties.InfluxConnection;
import com.myrrhax.usageservice.event.EnergyUsageEvent;
import com.myrrhax.usageservice.model.DeviceEnergy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@KafkaListener(topics = "energy-usage", containerFactory = "kafkaListenerContainerFactory")
@RequiredArgsConstructor
public class UsageService {
    private final InfluxDBClient influx;
    private final InfluxConnection influxConn;
    private final DeviceClient deviceClient;

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

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() throws ExecutionException, InterruptedException {
        Instant now = Instant.now();
        Instant hourAgo = now.minus(1, ChronoUnit.HOURS);
        String fluxQuery = String.format("""
        from (bucket: %s)
            |> range(start: time(v: %s), stop: time(v: %s))
            |> filter(fn: (r) => r["_measurement"] == "energy_usage")
            |> filter(fn: (r) => r["_field"] == "energyConsumed")
            |> group(columns: ["deviceId"])
            |> sum(column: "_value")
        """, influxConn.bucket(), hourAgo.toString(), now);

        QueryApi queryApi = influx.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxConn.org());
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record: table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                double energyConsumed = record.getValueByKey("_value") instanceof Number value
                        ? value.doubleValue()
                        : 0.0;
                deviceEnergies.add(DeviceEnergy.builder()
                            .deviceId(Long.valueOf(Objects.requireNonNullElse(deviceIdStr, "-1")))
                            .energyConsumed(energyConsumed)
                            .build());
            }
        }

        // ToDo: Add batching
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        for (DeviceEnergy deviceEnergy: deviceEnergies) {
            futures.add(
                    deviceClient.getDeviceById(deviceEnergy.getDeviceId())
                        .exceptionally(e -> {
                            log.error("Failed to fetch device with id {}", deviceEnergy.getDeviceId(), e);
                            return Optional.empty();
                        })
                        .thenAccept(deviceOpt -> {
                            deviceOpt.ifPresent(deviceDto -> deviceEnergy.setUserId(deviceDto.userId()));
                        })
            );
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();
        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream()
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User energies: {}", userDeviceEnergyMap);

        // Get user's energy consumption threshold
    }
}
