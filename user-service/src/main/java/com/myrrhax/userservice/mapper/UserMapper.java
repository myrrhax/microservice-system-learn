package com.myrrhax.userservice.mapper;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.CreateUserDto;
import com.myrrhax.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(UserDto dto);
    User toEntity(CreateUserDto request);

    UserDto toDto(User user);
}
