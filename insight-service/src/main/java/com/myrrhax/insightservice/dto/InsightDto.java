package com.myrrhax.insightservice.dto;

public record InsightDto(
        Long userId,
        String tips,
        double energyUsage
) { }
