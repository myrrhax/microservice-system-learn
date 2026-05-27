package com.myrrhax.usageservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEnergy {
    private Long deviceId;
    private double energyConsumed;
    private Long userId;
}
