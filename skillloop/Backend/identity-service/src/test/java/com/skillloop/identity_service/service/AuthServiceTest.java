package com.skillloop.identity_service.service;

import com.skillloop.identity_service.dto.RegisterRequest;
import com.skillloop.identity_service.entity.User;
import com.skillloop.identity_service.repository.RefreshTokenRepository;
import com.skillloop.identity_service.repository.UserRepository;
import com.skillloop.identity_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void register_refuseUnEmailDejaUtilise() {
        when(userRepository.existsByEmail("emna@esprit.tn")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(new RegisterRequest("emna@esprit.tn", "Motdepasse123", "Emna")));

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_creeUnEtudiantAvecUnMotDePasseHache() {
        when(userRepository.existsByEmail("emna@esprit.tn")).thenReturn(false);
        when(passwordEncoder.encode("Motdepasse123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.register(new RegisterRequest("Emna@Esprit.tn", "Motdepasse123", "Emna"));

        assertEquals("emna@esprit.tn", response.email());
        assertTrue(response.roles().contains("ETUDIANT"));
        verify(passwordEncoder).encode("Motdepasse123");
    }
}