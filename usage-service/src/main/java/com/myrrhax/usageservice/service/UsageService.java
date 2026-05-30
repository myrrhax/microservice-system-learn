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
import com.myrrhax.usageservice.dto.DeviceDto;
import com.myrrhax.usageservice.dto.UsageDto;
import com.myrrhax.usageservice.dto.UserDto;
import com.myrrhax.usageservice.event.AlertingEvent;
import com.myrrhax.usageservice.event.EnergyUsageEvent;
import com.myrrhax.usageservice.model.Device;
import com.myrrhax.usageservice.model.DeviceEnergy;
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
import java.util.Collections;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        log.info("Users thresholds: {}", userThresholdsMap);
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
        from (bucket: "%s")
            |> range(start: time(v: "%s"), stop: time(v: "%s"))
            |> filter(fn: (r) => r["_measurement"] == "energy_usage")
            |> filter(fn: (r) => r["_field"] == "energyConsumed")
            |> group(columns: ["deviceId"])
            |> sum(column: "_value")
        """, influxConn.bucket(), hourAgo.toString(), now);

        QueryApi queryApi = influx.getQueryApi();

        return queryApi.query(fluxQuery, influxConn.org());
    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days", userId, days);

        List<DeviceDto> deviceDtos = deviceClient.getAllDevicesForUser(userId);
        if (deviceDtos.isEmpty()) {
            return new UsageDto(userId, deviceDtos);
        }

        List<Device> devices = deviceDtos.stream()
                .map(dto -> new Device(dto.id(),
                        dto.name(),
                        dto.type(),
                        dto.location(),
                        dto.userId(),
                        dto.energyConsumed()))
                .toList();
        Instant now = Instant.now();
        Instant startTime = now.minus(days, ChronoUnit.DAYS);
        Map<Long, Double> deviceConsumptions = getDeviceConsumptions(startTime, now, devices);

        List<DeviceDto> result = new ArrayList<>(devices.size());
        for (Device device : devices) {
            device.setEnergyConsumed(deviceConsumptions.getOrDefault(device.getId(), 0.0));

            result.add(new DeviceDto(
                    device.getId(),
                    device.getName(),
                    device.getType(),
                    device.getLocation(),
                    device.getUserId(),
                    device.getEnergyConsumed()
            ));
        }

        return new UsageDto(userId, result);
    }

    private Map<Long, Double> getDeviceConsumptions(Instant startTime, Instant now, List<Device> devices) {
        Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            String devicesFilter = buildDeviceIdsFilter(devices);
            List<FluxTable> tables = getUserDeviceEnergyConsumptionsForDevices(startTime, now, devicesFilter);

            for (FluxTable table: tables) {
                for (FluxRecord record: table.getRecords()) {
                    String deviceIdStr = Objects.requireNonNullElse(record.getValueByKey("deviceId"), "-1")
                            .toString();
                    if (deviceIdStr.equals("-1")) {
                        continue;
                    }

                    double energyConsumed = record.getValueByKey("_value") instanceof Number value
                            ? value.doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr);
                    }
                }
            }

            return aggregatedMap;
        } catch (Exception e) {
            log.error("Failed to fetch user devices", e);

            return Collections.emptyMap();
        }
    }

    private static @NonNull String buildDeviceIdsFilter(List<Device> devices) {
        List<String> deviceIdsStrings = devices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
        return deviceIdsStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));
    }

    private @NonNull List<FluxTable> getUserDeviceEnergyConsumptionsForDevices(Instant startTime, Instant endTime, String filter) {
        String fluxQuery = String.format("""
                from(bucket: "%s")
                    |> range(start: time(v: "%s"), stop: time(v: "%s"))
                    |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                    |> filter(fn: (r) => r["_field"] == "energyConsumed")
                    |> filter(fn: (r) => %s)
                    |> group(columns: ["deviceId"])
                    |> sum(column: "_value")
                """, influxConn.bucket(), startTime, endTime, filter);
        QueryApi queryApi = influx.getQueryApi();

        return queryApi.query(fluxQuery, influxConn.org());
    }
}
