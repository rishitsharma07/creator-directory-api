package com.digitace.creator_directory_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCreatorRequest {

    @NotBlank
    private String name;

    private String niche;
}