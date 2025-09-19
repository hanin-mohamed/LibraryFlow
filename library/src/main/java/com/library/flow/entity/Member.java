package com.library.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable=false)
    private String fullName;
    @Column(unique=true)
    private String email;
    private String phone;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();
}
