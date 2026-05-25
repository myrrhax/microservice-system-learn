package com.myrrhax.deviceservice.exception;

import lombok.Getter;

@Getter
public class DeviceNotFoundException extends ApplicationException {
    private final Long id;

    public DeviceNotFoundException(Long id) {
        super("Device with id " + id + " is not found");
        this.id = id;
    }
}
