package com.skillloop.identity_service.service;

import com.skillloop.identity_service.dto.*;
import com.skillloop.identity_service.entity.*;
import com.skillloop.identity_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommunityResponse create(CreateCommunityRequest request) {
        String name = request.name().trim();
        if (communityRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Communauté déjà existante");
        }
        Community community = communityRepository.save(
                Community.builder().name(name).type(request.type()).build());
        return toResponse(community);
    }

    @Transactional
    public CommunityResponse join(UUID communityId, String userId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Communauté introuvable"));
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (community.getMembers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà membre de cette communauté");
        }
        community.getMembers().add(user);
        return toResponse(community);
    }

    private CommunityResponse toResponse(Community c) {
        return new CommunityResponse(c.getId(), c.getName(), c.getType().name(), c.getMembers().size());
    }
}