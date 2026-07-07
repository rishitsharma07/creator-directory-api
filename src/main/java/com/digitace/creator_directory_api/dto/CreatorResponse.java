package com.digitace.creator_directory_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreatorResponse {

    private UUID id;
    private String name;
    private String niche;
}