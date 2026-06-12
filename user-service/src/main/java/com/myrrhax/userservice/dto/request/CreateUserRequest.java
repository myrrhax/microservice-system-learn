package com.myrrhax.userservice.dto.request;

public record CreateUserRequest(
        String address,
        boolean alerting,
        double energyAlertingThreshold
) { }
