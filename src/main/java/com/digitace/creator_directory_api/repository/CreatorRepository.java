package com.digitace.creator_directory_api.repository;

import com.digitace.creator_directory_api.domain.Creator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, UUID> {
    // Left intentionally empty.
    // ALL read operations must go through AgencyCreatorLinkRepository to guarantee tenant isolation.
}