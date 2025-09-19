package com.library.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id @GeneratedValue private UUID id;

    @Column(nullable=false, length=240)
    private String title;

    @Column(unique=true, length=20)
    private String isbn;

    private Integer publicationYear;
    private String language;
    private Integer edition;
    @Column(columnDefinition="text")
    private String summary;
    @Column(columnDefinition="text")
    private String coverImageUrl;


    @Column(nullable=false)
    private Instant createdAt = Instant.now();

    @Column(nullable=false)
    private Integer totalCopies = 0;

    @Column(nullable=false)
    private Integer availableCopies = 0;
    @ManyToOne
    @JoinColumn(name="publisher_id")
    private Publisher publisher;

    @ManyToMany
    @JoinTable(name="book_author",
            joinColumns=@JoinColumn(name="book_id"),
            inverseJoinColumns=@JoinColumn(name="author_id"))
    private Set<Author> authors;

    @ManyToMany
    @JoinTable(name="book_category",
            joinColumns=@JoinColumn(name="book_id"),
            inverseJoinColumns=@JoinColumn(name="category_id"))
    private Set<Category> categories;



}
