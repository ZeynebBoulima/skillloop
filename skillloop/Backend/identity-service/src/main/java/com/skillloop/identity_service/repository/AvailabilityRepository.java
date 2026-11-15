package com.skillloop.identity_service.repository;

import com.skillloop.identity_service.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}