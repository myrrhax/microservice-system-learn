package com.myrrhax.deviceservice.client;

import com.myrrhax.deviceservice.dto.UserDto;

import java.util.Optional;

public interface UserClient {
    Optional<UserDto> getUserBySubId(String subId);
}
