package com.myrrhax.userservice.exception;

public class UserNotFoundException extends ApplicationException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }

    public UserNotFoundException() {
        super("User is not found");
    }
}