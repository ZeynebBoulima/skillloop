package com.skillloop.identity_service.service;

import com.skillloop.identity_service.dto.*;
import com.skillloop.identity_service.entity.*;
import com.skillloop.identity_service.repository.RefreshTokenRepository;
import com.skillloop.identity_service.repository.UserRepository;
import com.skillloop.identity_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.ETUDIANT))
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.fullName().trim())
                .build();
        user.setProfile(profile);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        if (user.getStatus() == UserStatus.SUSPENDU) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte suspendu");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expiré ou révoqué");
        }
        User user = stored.getUser();
        if (user.getStatus() == UserStatus.SUSPENDU) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte suspendu");
        }
        stored.setRevoked(true); // rotation : l'ancien jeton ne sert qu'une fois
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(t -> t.setRevoked(true));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return toResponse(user);
    }

    private AuthResponse issueTokens(User user) {
        RefreshToken refresh = RefreshToken.builder()
                .token(newRefreshTokenValue())
                .user(user)
                .expiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refresh);
        return new AuthResponse(jwtService.generateToken(user), refresh.getToken(),
                "Bearer", jwtService.getExpirationSeconds());
    }

    private String newRefreshTokenValue() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getProfile() != null ? user.getProfile().getFullName() : null,
                user.getStatus().name(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );
    }
}