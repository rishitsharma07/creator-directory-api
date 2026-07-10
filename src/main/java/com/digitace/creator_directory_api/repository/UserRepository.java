package com.digitace.creator_directory_api.repository;

import com.digitace.creator_directory_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Tenant-scoped: only ever returns users in the given agency.
    @Query("SELECT u FROM User u WHERE u.agency.id = :agencyId")
    List<User> findAllByAgencyId(@Param("agencyId") UUID agencyId);

    boolean existsByEmail(String email);
}