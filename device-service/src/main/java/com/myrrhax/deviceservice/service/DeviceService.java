package com.myrrhax.deviceservice.service;

import com.myrrhax.deviceservice.dto.DeviceDto;
import com.myrrhax.deviceservice.entity.Device;
import com.myrrhax.deviceservice.exception.DeviceNotFoundException;
import com.myrrhax.deviceservice.mapper.DeviceMapper;
import com.myrrhax.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public DeviceDto findById(Long id) {
        return deviceMapper.toDto(getDevice(id));
    }

    private Device getDevice(Long id) {
        return deviceRepository.findById(id)
                    .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}
