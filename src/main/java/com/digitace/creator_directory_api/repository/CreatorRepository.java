package com.digitace.creator_directory_api.repository;

import com.digitace.creator_directory_api.domain.Creator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorRepository extends JpaRepository<Creator, UUID> {
    // Creator has no agencyId -- it's a genuinely shared/global record by
    // design (see AgencyCreatorLink for where tenant-private data actually
    // lives: notes, agency linkage). Any query here can only ever return
    // shared fields (name, niche, follower count, email), never another
    // agency's private notes or link data -- so this doesn't violate
    // isolation. Reads that need to know "what does MY agency see" still
    // must go through AgencyCreatorLinkRepository.

    Optional<Creator> findByEmail(String email);
}