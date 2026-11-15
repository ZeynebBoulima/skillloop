package com.skillloop.identity_service.controller;

import com.skillloop.identity_service.dto.*;
import com.skillloop.identity_service.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponse create(@Valid @RequestBody CreateCommunityRequest request) {
        return communityService.create(request);
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponse join(@PathVariable UUID id, Authentication authentication) {
        return communityService.join(id, authentication.getName());
    }
}