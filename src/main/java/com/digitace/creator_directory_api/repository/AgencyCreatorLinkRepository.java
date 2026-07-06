package com.digitace.creator_directory_api.repository;

import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyCreatorLinkRepository extends JpaRepository<AgencyCreatorLink, UUID> {

    // 1. Retrieves all creators linked to the caller's agency.
    // The SpEL expression guarantees we NEVER fetch links belonging to other agencies.
    @Query("SELECT link FROM AgencyCreatorLink link JOIN FETCH link.creator " +
            "WHERE link.agency.id = :#{T(com.digitace.creator_directory_api.context.TenantContext).getAgencyId()}")
    Page<AgencyCreatorLink> findAllIsolated(Pageable pageable);

    // 2. Retrieves a single creator, but ONLY if the caller's agency has a link to it.
    @Query("SELECT link FROM AgencyCreatorLink link JOIN FETCH link.creator " +
            "WHERE link.creator.id = :creatorId " +
            "AND link.agency.id = :#{T(com.digitace.creator_directory_api.context.TenantContext).getAgencyId()}")
    Optional<AgencyCreatorLink> findIsolatedByCreatorId(@Param("creatorId") UUID creatorId);

    // 3. Counts how many creators an agency currently has (used later to enforce the 5-creator Free Plan limit)
    @Query("SELECT COUNT(link) FROM AgencyCreatorLink link " +
            "WHERE link.agency.id = :#{T(com.digitace.creator_directory_api.context.TenantContext).getAgencyId()}")
    long countIsolatedLinks();

    // 4. Search the caller's own linked creators, optionally filtered by niche
    // and/or minimum follower count. The tenant check is a plain top-level
    // AND with no OR anywhere near it, so it can never be bypassed by the
    // optional filters -- each optional filter is fully parenthesized as its
    // own independent "(:param IS NULL OR condition)" clause.
    @Query("SELECT link FROM AgencyCreatorLink link JOIN FETCH link.creator " +
            "WHERE link.agency.id = :#{T(com.digitace.creator_directory_api.context.TenantContext).getAgencyId()} " +
            "AND (:niche IS NULL OR link.creator.niche = :niche) " +
            "AND (:minFollowers IS NULL OR link.creator.followerCount >= :minFollowers)")
    Page<AgencyCreatorLink> searchIsolated(@Param("niche") String niche, @Param("minFollowers") Long minFollowers, Pageable pageable);

}