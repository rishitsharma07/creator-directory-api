package com.digitace.creator_directory_api.service;

import com.digitace.creator_directory_api.context.TenantContext;
import com.digitace.creator_directory_api.domain.Agency;
import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import com.digitace.creator_directory_api.domain.Creator;
import com.digitace.creator_directory_api.domain.PlanType;
import com.digitace.creator_directory_api.dto.CreateCreatorDto;
import com.digitace.creator_directory_api.dto.LinkCreatorDto;
import com.digitace.creator_directory_api.dto.UpdateCreatorDto;
import com.digitace.creator_directory_api.repository.AgencyCreatorLinkRepository;
import com.digitace.creator_directory_api.repository.AgencyRepository;
import com.digitace.creator_directory_api.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorService {

    private final AgencyRepository agencyRepository;
    private final CreatorRepository creatorRepository;
    private final AgencyCreatorLinkRepository linkRepository;

    // --- DAY 3: INGESTION ---
    @Transactional
    public void addCreatorToAgency(CreateCreatorDto dto) {
        UUID agencyId = TenantContext.getAgencyId();

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        if (agency.getPlan() == PlanType.FREE) {
            long currentCreatorCount = linkRepository.countIsolatedLinks();
            if (currentCreatorCount >= 5) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "FREE plan limit reached. Upgrade to PRO to add more creators.");
            }
        }

        Optional<Creator> existingCreator = creatorRepository.findByEmail(dto.getEmail());
        Creator creatorToLink;

        if (existingCreator.isPresent()) {
            creatorToLink = existingCreator.get();
            Optional<AgencyCreatorLink> existingLink = linkRepository.findIsolatedByCreatorId(creatorToLink.getId());
            if (existingLink.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Creator is already in your directory.");
            }
        } else {
            Creator newCreator = Creator.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .niche(dto.getNiche())
                    .followerCount((int) dto.getFollowerCount())
                    .engagementRate(dto.getEngagementRate())
                    .build();
            creatorToLink = creatorRepository.save(newCreator);
        }

        AgencyCreatorLink newLink = AgencyCreatorLink.builder()
                .agency(agency)
                .creator(creatorToLink)
                .notes(dto.getNotes())
                .build();

        linkRepository.save(newLink);
    }

    // --- DAY 4: MUTATION & SEARCH (New Logic) ---

    @Transactional
    public void updateCreator(UUID creatorId, UpdateCreatorDto dto) {
        // 1. Verify the agency has this creator in their isolated directory
        AgencyCreatorLink link = linkRepository.findIsolatedByCreatorId(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found in your directory"));

        // 2. Update the agency's private notes
        if (dto.getNotes() != null && !dto.getNotes().trim().isEmpty()) {
            link.setNotes(dto.getNotes());
        }

        // 3. Sync the global stats if provided
        Creator creator = link.getCreator();
        if (dto.getFollowerCount() != null) {
            creator.setFollowerCount(dto.getFollowerCount().intValue());
        }
        if (dto.getEngagementRate() != null) {
            creator.setEngagementRate(dto.getEngagementRate());
        }

        // 4. Save both the private link and the global creator
        creatorRepository.save(creator);
        linkRepository.save(link);
    }

    @Transactional
    public void removeCreatorFromDirectory(UUID creatorId) {
        // 1. Find the exact link securing this creator to this specific agency
        AgencyCreatorLink link = linkRepository.findIsolatedByCreatorId(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found in your directory"));

        // 2. GLOBAL CHECK: Is this the absolute last link keeping this creator alive?
        long totalLinks = linkRepository.countGlobalLinksByCreatorId(creatorId);

        // 3. SEVER PROTOCOL
        linkRepository.delete(link);

        // 4. CLEANUP: If we were the last link, destroy the orphaned creator record.
        if (totalLinks == 1) {
            creatorRepository.delete(link.getCreator());
        }
    }

    @Transactional(readOnly = true)
    public Page<AgencyCreatorLink> searchAgencyCreators(String niche, Long minFollowers, Long maxFollowers, Pageable pageable) {
        String searchNiche = (niche != null && !niche.trim().isEmpty()) ? niche : null;
        return linkRepository.searchIsolated(searchNiche, minFollowers, maxFollowers, pageable);
    }

    @Transactional
    public void linkExistingCreator(UUID creatorId, LinkCreatorDto dto) {
        UUID agencyId = TenantContext.getAgencyId();

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        // 1. Enforce Plan Limits
        if (agency.getPlan() == PlanType.FREE) {
            long currentCreatorCount = linkRepository.countIsolatedLinks();
            if (currentCreatorCount >= 5) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "FREE plan limit reached. Upgrade to PRO to add more creators.");
            }
        }

        // 2. Verify creator actually exists globally
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator does not exist in the global directory."));

        // 3. Verify the agency isn't already linked to this creator
        Optional<AgencyCreatorLink> existingLink = linkRepository.findIsolatedByCreatorId(creatorId);
        if (existingLink.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Creator is already linked to your directory.");
        }

        // 4. Create and save the isolated link
        AgencyCreatorLink newLink = AgencyCreatorLink.builder()
                .agency(agency)
                .creator(creator)
                .notes(dto.getNotes())
                .build();

        linkRepository.save(newLink);
    }
}
