package com.myrrhax.insightservice.dto;

public record DeviceDto(
        Long id,
        String name,
        String type,
        String location,
        double energyConsumed
) { }
