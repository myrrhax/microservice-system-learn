package com.myrrhax.deviceservice.mapper;

import com.myrrhax.deviceservice.dto.DeviceDto;
import com.myrrhax.deviceservice.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceMapper {
    DeviceDto toDto(Device device);
}
