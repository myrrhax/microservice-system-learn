package com.myrrhax.deviceservice.exception;

public class DeviceNotFoundException extends ApplicationException {
    public DeviceNotFoundException(Long id) {
        super("Device with id " + id + " is not found");
    }
}
