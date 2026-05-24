package com.myrrhax.deviceservice.dto.request;

import com.myrrhax.deviceservice.model.DeviceType;

public record UpdateDeviceRequest(
        String name,
        DeviceType type,
        String location,
        Long userId
) { }
