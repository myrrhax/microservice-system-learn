package com.myrrhax.deviceservice.controller;

import com.myrrhax.deviceservice.dto.DeviceDto;
import com.myrrhax.deviceservice.dto.request.CreateDeviceRequest;
import com.myrrhax.deviceservice.dto.request.UpdateDeviceRequest;
import com.myrrhax.deviceservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping("{id}")
    public ResponseEntity<DeviceDto> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(
                deviceService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody CreateDeviceRequest deviceDto) {
        return ResponseEntity.ok(
                deviceService.createDevice(deviceDto)
        );
    }

    @PutMapping("{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long id,
                                                  @RequestBody UpdateDeviceRequest deviceDto) {
        return ResponseEntity.ok(
                deviceService.updateDevice(id, deviceDto)
        );
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);

        return ResponseEntity.noContent().build();
    }
}
