package com.myrrhax.usageservice.dto;

import java.util.List;

public record UsageDto(
        Long userId,
        List<DeviceDto> devices
) { }
