package com.skillloop.identity_service.controller;

import com.skillloop.identity_service.dto.*;
import com.skillloop.identity_service.service.AuthService;
import com.skillloop.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    @PutMapping("/me/profile")
    public ProfileResponse updateProfile(Authentication authentication,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @PutMapping("/me/availabilities")
    public List<AvailabilityDto> setAvailabilities(Authentication authentication,
                                                   @Valid @RequestBody List<@Valid AvailabilityDto> items) {
        return userService.setAvailabilities(authentication.getName(), items);
    }

    @GetMapping("/{id}/public-profile")
    public ProfileResponse publicProfile(@PathVariable UUID id) {
        return userService.getPublicProfile(id);
    }

    @GetMapping("/{id}/availabilities")
    public List<AvailabilityDto> availabilities(@PathVariable UUID id) {
        return userService.getAvailabilities(id);
    }
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('MODERATEUR','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspend(@PathVariable UUID id) {
        userService.suspend(id);
    }
}