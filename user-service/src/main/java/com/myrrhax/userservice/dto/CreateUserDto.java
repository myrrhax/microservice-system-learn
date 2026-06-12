package com.myrrhax.userservice.dto;

public record CreateUserDto(
        String subId,
        String name,
        String surname,
        String email,
        String address,
        boolean alerting,
        double energyAlertingThreshold
) { }
