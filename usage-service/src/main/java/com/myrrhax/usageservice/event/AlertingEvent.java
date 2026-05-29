package com.myrrhax.usageservice.event;

public record AlertingEvent(
        Long userId,
        String message,
        double threshold,
        double energyConsumed,
        String email
) { }