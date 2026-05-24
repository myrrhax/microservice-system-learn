package com.myrrhax.userservice.controller;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.request.CreateUserRequest;
import com.myrrhax.userservice.dto.request.UpdateUserRequest;
import com.myrrhax.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest dto) {
        UserDto createdUser = userService.createUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                userService.getUser(id)
        );
    }

    @PutMapping("{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @RequestBody UpdateUserRequest dto) {
        return ResponseEntity.ok(
                userService.updateUser(id, dto)
        );
    }
}
