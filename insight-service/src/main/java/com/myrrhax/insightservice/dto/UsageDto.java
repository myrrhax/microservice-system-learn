package com.myrrhax.insightservice.dto;

import java.util.List;

public record UsageDto(
        Long userId,
        List<DeviceDto> devices
) {
}
