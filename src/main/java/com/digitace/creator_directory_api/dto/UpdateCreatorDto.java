package com.digitace.creator_directory_api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateCreatorDto {

    private String notes;

    @PositiveOrZero
    private Long followerCount;

    @PositiveOrZero
    private Double engagementRate;

}
