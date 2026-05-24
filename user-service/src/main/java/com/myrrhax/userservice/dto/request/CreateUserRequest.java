package com.myrrhax.userservice.dto.request;

public record CreateUserRequest(
        String name,
        String surname,
        String email,
        String address,
        boolean alerting,
        double energyAlertingThreshold
) { }
