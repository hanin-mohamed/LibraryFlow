package com.library.flow.entity;


import com.library.flow.common.dto.BorrowStatus;
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
public class BorrowBook {

    @Id
    @GeneratedValue private UUID id;

    @Column(nullable=false)
    private Instant borrowedAt = Instant.now();

    @Column(nullable=false)
    private Instant dueAt;
    private Instant returnedAt;

    @ManyToOne(optional=false)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(optional=false)
    @JoinColumn(name="book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BorrowStatus status;
}
