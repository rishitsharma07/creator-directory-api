package com.digitace.creator_directory_api.controller;

import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import com.digitace.creator_directory_api.repository.AgencyCreatorLinkRepository;
import com.digitace.creator_directory_api.service.CreatorService;
import com.digitace.creator_directory_api.dto.CreatorResponse;
import com.digitace.creator_directory_api.dto.UpdateCreatorDto;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.digitace.creator_directory_api.dto.CreateCreatorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final AgencyCreatorLinkRepository linkRepository;
    private final CreatorService creatorService;
    @GetMapping
    public ResponseEntity<?> getCreators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // 1. Fetch the isolated links (SQL automatically filters by the current Agency ID in the vault)
        Page<AgencyCreatorLink> links = linkRepository.findAllIsolated(PageRequest.of(page, limit));

        // 2. Map the vault objects directly into the strict JSON format the challenge requires
        List<Map<String, Object>> response = links.stream().map(link -> Map.of(
                "id", link.getCreator().getId(),
                "name", link.getCreator().getName(),
                "niche", link.getCreator().getNiche(),
                "followerCount", link.getCreator().getFollowerCount(),
                "engagementRate", link.getCreator().getEngagementRate(),
                "email", link.getCreator().getEmail(),

                // We lock the array to ONLY contain this specific agency's link.
                // A data leak is structurally impossible here.
                "agencyLinks", List.of(Map.of(
                        "agencyId", link.getAgency().getId(),
                        "notes", link.getNotes() != null ? link.getNotes() : "",
                        "addedAt", link.getAddedAt()
                ))
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<CreatorResponse> createCreator(
            @Valid @RequestBody CreateCreatorRequest request) {

        CreatorResponse response = creatorService.createCreator(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCreator(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCreatorDto request) {

        return ResponseEntity.ok(
                creatorService.updateCreator(id, request)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreator(
            @PathVariable UUID id) {

        creatorService.deleteCreator(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchCreators(

            @RequestParam(required = false) String niche,

            @RequestParam(required = false) Integer minFollowers) {

        return ResponseEntity.ok(
                creatorService.searchCreators(niche, minFollowers)
        );
    }
}