package com.skillloop.identity_service.repository;

import com.skillloop.identity_service.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommunityRepository extends JpaRepository<Community, UUID> {
    boolean existsByName(String name);
}