package com.myrrhax.userservice.dto.request;

public record UpdateUserRequest(
        String name,
        String surname,
        String email,
        String address,
        boolean alerting,
        double energyAlertingThreshold
) { }
