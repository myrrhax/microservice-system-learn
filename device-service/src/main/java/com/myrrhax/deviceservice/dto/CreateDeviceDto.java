package com.myrrhax.deviceservice.dto;

import com.myrrhax.deviceservice.model.DeviceType;

public record CreateDeviceDto(
        String name,
        DeviceType type,
        String location,
        String userSubId
) { }
