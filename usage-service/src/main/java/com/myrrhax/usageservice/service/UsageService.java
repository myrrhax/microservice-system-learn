package com.myrrhax.usageservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.myrrhax.usageservice.client.DeviceClient;
import com.myrrhax.usageservice.client.UserClient;
import com.myrrhax.usageservice.config.properties.InfluxConnection;
import com.myrrhax.usageservice.dto.UserDto;
import com.myrrhax.usageservice.event.AlertingEvent;
import com.myrrhax.usageservice.event.EnergyUsageEvent;
import com.myrrhax.usageservice.model.DeviceEnergy;
import io.reactivex.rxjava3.internal.functions.Functions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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
    // ToDo Use MessageSource and save user preferred localization
    private static final String THRESHOLD_EXCEEDED_MESSAGE_PATTERN = "Energy consumption threshold exceeded";

    private final InfluxDBClient influx;
    private final InfluxConnection influxConn;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${app.consumption-interval:1}")
    private long energyConsumptionIntervalHours;


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
        List<FluxTable> tables = getLastEnergyConsumptionsGrouped(energyConsumptionIntervalHours);
        List<DeviceEnergy> deviceConsumptions = getDeviceConsumptions(tables);
        fetchDeviceInfo(deviceConsumptions);

        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceConsumptions.stream()
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));
        log.info("User energies: {}", userDeviceEnergyMap);

        // Get user's energy consumption threshold
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        Map<Long, Double> userThresholdsMap = new HashMap<>();
        Map<Long, String> userEmailsMap = new HashMap<>();

        fetchUserInfo(userIds, userThresholdsMap, userEmailsMap);

        // Check thresholds
        List<Long> alertedUsers = new ArrayList<>(userThresholdsMap.keySet());

        for (Long userId: alertedUsers) {
            double threshold = userThresholdsMap.get(userId);
            double consumedEnergy = userDeviceEnergyMap.get(userId).stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();
            if (consumedEnergy > threshold) {
                log.info("ALERT: user {} has exceeded the energy threshold. Total consumption is {}, Threshold is {}",
                        userId, threshold, consumedEnergy);
                AlertingEvent event = new AlertingEvent(
                        userId,
                        THRESHOLD_EXCEEDED_MESSAGE_PATTERN,
                        threshold,
                        consumedEnergy,
                        userEmailsMap.get(userId)
                );
                kafkaTemplate.send("energy-alerts", event);
            }
        }
    }

    private void fetchUserInfo(List<Long> userIds, Map<Long, Double> userThresholdsMap, Map<Long, String> userEmailsMap) throws InterruptedException, ExecutionException {
        List<CompletableFuture<Optional<UserDto>>> userFetchFutures = new LinkedList<>();
        for (Long userId: userIds) {
            userFetchFutures.add(
                    userClient.getUserById(userId)
                            .exceptionally(e -> {
                                log.error("Failed to fetch user with id {}", userId, e);
                                return Optional.empty();
                            })
            );
        }
        CompletableFuture.allOf(userFetchFutures.toArray(new CompletableFuture[0]))
                .join();

        for (CompletableFuture<Optional<UserDto>> getUserTask: userFetchFutures) {
            Optional<UserDto> userOpt = getUserTask.get();
            if (userOpt.isEmpty()) {
                continue;
            }
            UserDto user = userOpt.get();
            if (!user.alerting()) {
                continue;
            }
            userThresholdsMap.put(user.id(), user.energyAlertingThreshold());
            userEmailsMap.put(user.id(), user.email());
        }
    }

    private void fetchDeviceInfo(List<DeviceEnergy> deviceConsumptions) {
        // ToDo: Add batching
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        for (DeviceEnergy deviceEnergy: deviceConsumptions) {
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
        deviceConsumptions.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);
    }

    private static @NonNull List<DeviceEnergy> getDeviceConsumptions(List<FluxTable> tables) {
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
        return deviceEnergies;
    }

    private @NonNull List<FluxTable> getLastEnergyConsumptionsGrouped(long passedHours) {
        Instant now = Instant.now();
        Instant hourAgo = now.minus(passedHours, ChronoUnit.HOURS);
        String fluxQuery = String.format("""
        from (bucket: %s)
            |> range(start: time(v: %s), stop: time(v: %s))
            |> filter(fn: (r) => r["_measurement"] == "energy_usage")
            |> filter(fn: (r) => r["_field"] == "energyConsumed")
            |> group(columns: ["deviceId"])
            |> sum(column: "_value")
        """, influxConn.bucket(), hourAgo.toString(), now);

        QueryApi queryApi = influx.getQueryApi();

        return queryApi.query(fluxQuery, influxConn.org());
    }
}
