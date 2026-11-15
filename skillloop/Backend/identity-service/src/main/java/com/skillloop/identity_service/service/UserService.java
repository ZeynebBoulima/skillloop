package com.skillloop.identity_service.service;

import com.skillloop.identity_service.dto.*;
import com.skillloop.identity_service.entity.*;
import com.skillloop.identity_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UUID id = UUID.fromString(userId);
        try {
            ZoneId.of(request.timezone());
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuseau horaire invalide");
        }
        Profile profile = profileRepository.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil introuvable"));

        profile.setFullName(request.fullName().trim());
        profile.setTimezone(request.timezone());
        profile.getLanguages().clear();
        if (request.languages() != null) {
            profile.getLanguages().addAll(request.languages());
        }
        return toResponse(id, profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(UUID id) {
        Profile profile = profileRepository.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return toResponse(id, profile);
    }

    @Transactional
    public List<AvailabilityDto> setAvailabilities(String userId, List<AvailabilityDto> items) {
        UUID id = UUID.fromString(userId);
        for (AvailabilityDto a : items) {
            if (!a.startTime().isBefore(a.endTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'heure de début doit précéder l'heure de fin");
            }
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        availabilityRepository.deleteByUserId(id);
        availabilityRepository.flush();

        List<Availability> entities = items.stream()
                .map(a -> Availability.builder()
                        .user(user)
                        .dayOfWeek(a.dayOfWeek())
                        .startTime(a.startTime())
                        .endTime(a.endTime())
                        .build())
                .toList();
        availabilityRepository.saveAll(entities);
        return items;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailabilities(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        return availabilityRepository.findByUserId(userId).stream()
                .map(a -> new AvailabilityDto(a.getDayOfWeek(), a.getStartTime(), a.getEndTime()))
                .toList();
    }

    private ProfileResponse toResponse(UUID userId, Profile p) {
        return new ProfileResponse(userId, p.getFullName(), new HashSet<>(p.getLanguages()), p.getTimezone());
    }
    @Transactional
    public void suspend(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        user.setStatus(UserStatus.SUSPENDU);
        refreshTokenRepository.findByUserIdAndRevokedFalse(id).forEach(t -> t.setRevoked(true));
    }
}