package com.myrrhax.userservice.controller;

import com.myrrhax.userservice.dto.UserDto;
import com.myrrhax.userservice.dto.CreateUserDto;
import com.myrrhax.userservice.dto.request.CreateUserRequest;
import com.myrrhax.userservice.dto.request.UpdateUserRequest;
import com.myrrhax.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(Authentication authentication,
                                              @RequestBody CreateUserRequest request) {
        Jwt jwt = Objects.requireNonNull((Jwt) authentication.getPrincipal());
        UserDto createdUser = userService.createUser(new CreateUserDto(
                jwt.getSubject(),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("email"),
                request.address(),
                request.alerting(),
                request.energyAlertingThreshold()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping("me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                userService.getUserBySubId(jwt.getSubject())
        );
    }

    @GetMapping("{id:\\d+}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                userService.getUser(id)
        );
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUserBySubId(@PathVariable String id) {
        return ResponseEntity.ok(
                userService.getUserBySubId(id)
        );
    }

    @PutMapping("{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @RequestBody UpdateUserRequest dto) {
        return ResponseEntity.ok(
                userService.updateUser(id, dto)
        );
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.ok().build();
    }
}
