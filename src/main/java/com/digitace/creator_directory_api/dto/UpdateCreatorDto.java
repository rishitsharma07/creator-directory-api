package com.digitace.creator_directory_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCreatorDto {

    @NotBlank
    private String name;

    private String niche;

    private Integer followerCount;

    @Min(0)
    @Max(100)
    private Double engagementRate;

    @Email
    private String email;

    private String notes;
}