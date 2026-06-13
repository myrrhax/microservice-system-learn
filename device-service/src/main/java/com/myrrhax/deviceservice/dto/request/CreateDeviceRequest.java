package com.myrrhax.deviceservice.dto.request;

import com.myrrhax.deviceservice.model.DeviceType;

public record CreateDeviceRequest(
        String name,
        DeviceType type,
        String location
) { }