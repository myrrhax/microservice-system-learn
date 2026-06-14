package com.myrrhax.deviceservice.repository;

import com.myrrhax.deviceservice.entity.Device;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeviceRepository extends CrudRepository<Device, Long> {
    List<Device> findAllByUserId(Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
