package com.digitace.creator_directory_api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "creators")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Creator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String niche;
    private Integer followerCount;
    private Double engagementRate;
    private String email;
}
