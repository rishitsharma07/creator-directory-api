package com.digitace.creator_directory_api.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
