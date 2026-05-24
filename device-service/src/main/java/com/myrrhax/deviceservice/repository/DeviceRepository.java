package com.myrrhax.deviceservice.repository;

import com.myrrhax.deviceservice.entity.Device;
import org.springframework.data.repository.CrudRepository;

public interface DeviceRepository extends CrudRepository<Device, Long> {

}
