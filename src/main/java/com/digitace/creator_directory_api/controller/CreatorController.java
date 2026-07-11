package com.digitace.creator_directory_api.controller;

import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import com.digitace.creator_directory_api.dto.CreateCreatorDto;
import com.digitace.creator_directory_api.dto.LinkCreatorDto;
import com.digitace.creator_directory_api.dto.UpdateCreatorDto;
import com.digitace.creator_directory_api.repository.AgencyCreatorLinkRepository;
import com.digitace.creator_directory_api.service.CreatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final AgencyCreatorLinkRepository linkRepository;
    private final CreatorService creatorService;

    // --- DAY 2 & 4: UNIFIED READ ENGINE (List, Filter, Sort) ---
    @GetMapping
    public ResponseEntity<?> getCreators(
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) Long minFollowers,
            @RequestParam(required = false) Long maxFollowers,
            @RequestParam(defaultValue = "followerCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // Determine sort direction
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Map external field name to internal entity path for Spring Data
        String sortProperty = sortBy.equals("followerCount") ? "creator.followerCount" : sortBy;

        // Create the PageRequest with sorting baked in
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by(direction, sortProperty));

        // Execute unified search
        Page<AgencyCreatorLink> links = creatorService.searchAgencyCreators(niche, minFollowers, maxFollowers, pageRequest);

        List<Map<String, Object>> response = links.stream()
                .map(this::toCreatorResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCreatorById(@PathVariable UUID id) {
        return linkRepository.findIsolatedByCreatorId(id)
                .map(this::toCreatorResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- DAY 3: THE INGESTION ENGINE ---
    @PostMapping
    public ResponseEntity<?> addCreator(@Valid @RequestBody CreateCreatorDto dto) {
        creatorService.addCreatorToAgency(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Creator successfully added to your directory."));
    }

    // The newly added Linking Endpoint
    @PostMapping("/{id}/link")
    public ResponseEntity<?> linkCreator(@PathVariable UUID id, @RequestBody(required = false) LinkCreatorDto dto) {
        if (dto == null) {
            dto = new LinkCreatorDto();
        }
        creatorService.linkExistingCreator(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Creator successfully linked to your directory."));
    }

    // --- DAY 4: MUTATION ---
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCreator(@PathVariable UUID id, @Valid @RequestBody UpdateCreatorDto dto) {
        creatorService.updateCreator(id, dto);
        return ResponseEntity.ok(Map.of("message", "Creator updated successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreator(@PathVariable UUID id) {
        creatorService.removeCreatorFromDirectory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Shared JSON shape for a single creator
     */
    private Map<String, Object> toCreatorResponse(AgencyCreatorLink link) {
        return Map.of(
                "id", link.getCreator().getId(),
                "name", link.getCreator().getName(),
                "niche", link.getCreator().getNiche(),
                "followerCount", link.getCreator().getFollowerCount(),
                "engagementRate", link.getCreator().getEngagementRate(),
                "email", link.getCreator().getEmail(),
                "agencyLinks", List.of(Map.of(
                        "agencyId", link.getAgency().getId(),
                        "notes", link.getNotes() != null ? link.getNotes() : "",
                        "addedAt", link.getAddedAt()
                ))
        );
    }
}