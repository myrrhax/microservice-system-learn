package com.myrrhax.deviceservice.service;

import com.myrrhax.deviceservice.entity.Device;
import com.myrrhax.deviceservice.model.DeviceType;
import com.myrrhax.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@Transactional
@Profile("dev")
@RequiredArgsConstructor
public class DevDataFillerService implements ApplicationRunner {
    private static final int NUMBER_OF_DEVICES = 200;
    private static final int NUMBER_OF_USERS = 10;
    private final DeviceRepository deviceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Populating device data...");
        if (deviceRepository.count() > 0) {
            log.info("Database already has device information");
            return;
        }

        List<Device> devices = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_DEVICES; i++) {
            var device = Device.builder()
                    .name("Device" + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("Location" + ((i % 3) + 1))
                    .userId((long) ((i % NUMBER_OF_USERS) + 1))
                    .build();
            devices.add(device);
        }

        deviceRepository.saveAll(devices);
        log.info("Device repository has been populated with {} devices", NUMBER_OF_DEVICES);
    }
}
