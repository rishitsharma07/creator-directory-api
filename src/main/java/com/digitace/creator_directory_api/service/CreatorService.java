package com.digitace.creator_directory_api.service;

import com.digitace.creator_directory_api.context.TenantContext;
import com.digitace.creator_directory_api.domain.Agency;
import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import com.digitace.creator_directory_api.domain.Creator;
import com.digitace.creator_directory_api.domain.PlanType;
import com.digitace.creator_directory_api.dto.CreateCreatorDto;
import com.digitace.creator_directory_api.repository.AgencyCreatorLinkRepository;
import com.digitace.creator_directory_api.repository.AgencyRepository;
import com.digitace.creator_directory_api.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void addCreatorToAgency(CreateCreatorDto dto) {
        UUID agencyId = TenantContext.getAgencyId();

        // 1. The Limit Check
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        if (agency.getPlan() == PlanType.FREE) {
            long currentCreatorCount = linkRepository.countIsolatedLinks();
            if (currentCreatorCount >= 5) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "FREE plan limit reached. Upgrade to PRO to add more creators.");
            }
        }

        // 2. The Global Search
        Optional<Creator> existingCreator = creatorRepository.findByEmail(dto.getEmail());

        Creator creatorToLink;

        if (existingCreator.isPresent()) {
            // 3A. The Fork: Creator exists. We only link them.
            creatorToLink = existingCreator.get();

            // Safety check: Prevent the agency from adding the exact same creator twice
            Optional<AgencyCreatorLink> existingLink = linkRepository.findIsolatedByCreatorId(creatorToLink.getId());
            if (existingLink.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Creator is already in your directory.");
            }
        } else {
            // 3B. The Fork: Brand new creator. Save to global database first.
            Creator newCreator = Creator.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .niche(dto.getNiche())
                    // Fix: Explicitly cast Member 1's long to match the database's integer
                    .followerCount((int) dto.getFollowerCount())
                    .engagementRate(dto.getEngagementRate())
                    .build();
            creatorToLink = creatorRepository.save(newCreator);
        }

        // 4. Secure the link with the agency's private notes
        AgencyCreatorLink newLink = AgencyCreatorLink.builder()
                .agency(agency)
                .creator(creatorToLink)
                .notes(dto.getNotes())
                .build();

        linkRepository.save(newLink);
    }
}