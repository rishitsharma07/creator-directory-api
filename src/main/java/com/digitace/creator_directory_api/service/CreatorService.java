package com.digitace.creator_directory_api.service;

import com.digitace.creator_directory_api.domain.Creator;
import com.digitace.creator_directory_api.dto.CreateCreatorRequest;
import com.digitace.creator_directory_api.dto.CreatorResponse;
import com.digitace.creator_directory_api.repository.CreatorRepository;
import com.digitace.creator_directory_api.dto.UpdateCreatorDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatorService {

    private final CreatorRepository creatorRepository;

    public CreatorResponse createCreator(CreateCreatorRequest request) {

        Creator creator = Creator.builder()
                .name(request.getName())
                .niche(request.getNiche())
                .build();

        Creator savedCreator = creatorRepository.save(creator);

        return CreatorResponse.builder()
                .id(savedCreator.getId())
                .name(savedCreator.getName())
                .niche(savedCreator.getNiche())
                .build();
    }
    public Object updateCreator(UUID id, UpdateCreatorDto request) {

        return null;
    }
    public void deleteCreator(UUID id) {

    }
    public Object searchCreators(String niche, Integer minFollowers) {

        return null;
    }
}