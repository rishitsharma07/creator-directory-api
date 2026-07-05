package com.digitace.creator_directory_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateCreatorDto {

    @NotBlank(message= "Name is Required")
    private String name;

    @NotBlank(message = "Niche is required")
    private String niche;

    @PositiveOrZero(message = "Follower count cannot be negative")
    private long followerCount;

    @PositiveOrZero(message = "Engagement rate cannot be negative")
    private double engagementRate;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String notes;
}
