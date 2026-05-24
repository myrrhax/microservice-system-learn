package com.myrrhax.deviceservice.dto;

import com.myrrhax.deviceservice.model.DeviceType;

public record DeviceDto(
        Long id,
        String name,
        DeviceType type,
        String location,
        Long userId
) { }
