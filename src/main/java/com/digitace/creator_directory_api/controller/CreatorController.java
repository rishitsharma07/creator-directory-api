package com.digitace.creator_directory_api.controller;

import com.digitace.creator_directory_api.domain.AgencyCreatorLink;
import com.digitace.creator_directory_api.dto.CreateCreatorDto;
import com.digitace.creator_directory_api.repository.AgencyCreatorLinkRepository;
import com.digitace.creator_directory_api.service.CreatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

    // --- DAY 2: THE READ ENGINE ---
    @GetMapping
    public ResponseEntity<?> getCreators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Page<AgencyCreatorLink> links = linkRepository.findAllIsolated(PageRequest.of(page, limit));

        List<Map<String, Object>> response = links.stream().map(link -> Map.of(
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
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // --- DAY 3: THE INGESTION ENGINE ---
    @PostMapping
    public ResponseEntity<?> addCreator(@Valid @RequestBody CreateCreatorDto dto) {
        // The service automatically handles Free Plan limits, Global Search, and Tenant Isolation
        creatorService.addCreatorToAgency(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Creator successfully added to your directory."));
    }
}