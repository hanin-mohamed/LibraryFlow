package com.library.flow.service;

import com.library.flow.entity.Author;
import com.library.flow.entity.Book;
import com.library.flow.entity.Category;
import com.library.flow.entity.Publisher;
import com.library.flow.repository.AuthorRepository;
import com.library.flow.repository.BookRepository;
import com.library.flow.repository.CategoryRepository;
import com.library.flow.repository.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    @Transactional
    public UUID addBook(Book book) {
        log.info("addBook: start");
        if (book == null) {
            log.warn("addBook: body is null");
            throw new IllegalArgumentException("body is required");
        }
        book.setId(null);

        if (isBlank(book.getTitle())) {
            log.warn("addBook: title is blank");
            throw new IllegalArgumentException("title is required");
        }
        if (book.getCreatedAt() == null) book.setCreatedAt(Instant.now());

        if (book.getPublisher() == null || book.getPublisher().getId() == null) {
            log.warn("addBook: publisher.id missing");
            throw new IllegalArgumentException("publisher.id is required");
        }
        book.setPublisher(resolvePublisher(book.getPublisher().getId()));

        book.setAuthors(resolveAuthorsRequired(book.getAuthors()));
        log.info("addBook: authors resolved count={}", book.getAuthors().size());

        book.setCategories(resolveCategoriesRequired(book.getCategories()));
        log.info("addBook: categories resolved count={}", book.getCategories().size());

        normalizeAndValidateCopies(book);
        log.info("addBook: copies total={}, available={}", book.getTotalCopies(), book.getAvailableCopies());

        bookRepository.save(book);
        log.info("addBook: saved id={}", book.getId());
        return book.getId();
    }

    @Transactional
    public void updateBook(UUID id, Book request) {
        log.info("updateBook: id={}", id);
        if (id == null) {
            log.warn("updateBook: id is null");
            throw new IllegalArgumentException("id is required");
        }

        Book book = bookRepository.findById(id).orElseThrow(() -> {
            log.error("updateBook: book not found id={}", id);
            return new EntityNotFoundException("book");
        });

        if (!isBlank(request.getTitle())) book.setTitle(request.getTitle());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getLanguage() != null) book.setLanguage(request.getLanguage());
        if (request.getEdition() != null) book.setEdition(request.getEdition());
        if (request.getSummary() != null) book.setSummary(request.getSummary());
        if (request.getCoverImageUrl() != null) book.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getPublisher() != null) {
            UUID pid = request.getPublisher().getId();
            if (pid == null) {
                log.warn("updateBook: publisher provided without id");
                throw new IllegalArgumentException("publisher.id is required when publisher is provided");
            }
            book.setPublisher(resolvePublisher(pid));
            log.info("updateBook: publisher updated id={}", pid);
        }

        if (request.getAuthors() != null) {
            Set<Author> authors = resolveAuthorsRequired(request.getAuthors());
            book.getAuthors().clear();
            book.getAuthors().addAll(authors);
            log.info("updateBook: authors replaced count={}", authors.size());
        }

        if (request.getCategories() != null) {
            Set<Category> cats = resolveCategoriesRequired(request.getCategories());
            book.getCategories().clear();
            book.getCategories().addAll(cats);
            log.info("updateBook: categories replaced count={}", cats.size());
        }

        Integer newTotal = request.getTotalCopies();
        Integer newAvail = request.getAvailableCopies();
        if (newTotal != null || newAvail != null) {
            int total = (newTotal != null) ? newTotal : (book.getTotalCopies() != null ? book.getTotalCopies() : 0);
            int avail = (newAvail != null) ? newAvail : (book.getAvailableCopies() != null ? book.getAvailableCopies() : 0);
            if (total < 0 || avail < 0) {
                log.warn("updateBook: negative copies total={}, available={}", total, avail);
                throw new IllegalArgumentException("copies must be >= 0");
            }
            if (avail > total) {
                log.warn("updateBook: availableCopies {} > totalCopies {}", avail, total);
                throw new IllegalArgumentException("availableCopies cannot exceed totalCopies");
            }
            book.setTotalCopies(total);
            book.setAvailableCopies(avail);
            log.info("updateBook: copies updated total={}, available={}", total, avail);
        }

        log.info("updateBook: done id={}", id);
    }

    private Publisher resolvePublisher(UUID pid) {
        return publisherRepository.findById(pid).orElseThrow(() -> {
            log.error("resolvePublisher: not found id={}", pid);
            return new EntityNotFoundException("publisher not found: " + pid);
        });
    }

    private Set<Author> resolveAuthorsRequired(Set<Author> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            log.warn("resolveAuthorsRequired: empty");
            throw new IllegalArgumentException("at least one author is required");
        }

        Set<UUID> ids = new HashSet<>();
        for (Author a : incoming) {
            if (a == null || a.getId() == null) {
                log.warn("resolveAuthorsRequired: author without id");
                throw new IllegalArgumentException("author.id is required");
            }
            ids.add(a.getId());
        }

        List<Author> found = authorRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            log.warn("resolveAuthorsRequired: some ids not found requested={}, found={}", ids.size(), found.size());
        }
        return new HashSet<>(found);
    }

    private Set<Category> resolveCategoriesRequired(Set<Category> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            log.warn("resolveCategoriesRequired: empty");
            throw new IllegalArgumentException("at least one category is required");
        }

        Set<UUID> ids = new HashSet<>();
        for (Category c : incoming) {
            if (c == null || c.getId() == null) {
                log.warn("resolveCategoriesRequired: category without id");
                throw new IllegalArgumentException("category.id is required");
            }
            ids.add(c.getId());
        }

        List<Category> found = categoryRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            log.warn("resolveCategoriesRequired: some ids not found requested={}, found={}", ids.size(), found.size());
        }
        return new HashSet<>(found);
    }

    private void normalizeAndValidateCopies(Book b) {
        int total = (b.getTotalCopies() != null) ? b.getTotalCopies() : 0;
        int avail = (b.getAvailableCopies() != null) ? b.getAvailableCopies() : total;
        if (total < 0 || avail < 0) {
            log.warn("normalizeAndValidateCopies: negative values total={}, available={}", total, avail);
            throw new IllegalArgumentException("copies must be >= 0");
        }
        if (avail > total) {
            log.warn("normalizeAndValidateCopies: availableCopies {} > totalCopies {}", avail, total);
            throw new IllegalArgumentException("availableCopies cannot exceed totalCopies");
        }
        b.setTotalCopies(total);
        b.setAvailableCopies(avail);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public Page<Book> findAll(Pageable pageable) {
        log.debug("findAll: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable);
    }

    public void deleteByUUID(UUID id) {
        log.info("deleteByUUID: id={}", id);
        if (id == null) {
            log.warn("deleteByUUID: id is null");
            throw new IllegalArgumentException("id is required");
        }
        if (!bookRepository.existsById(id)) {
            log.error("deleteByUUID: not found id={}", id);
            throw new EntityNotFoundException("book not found: " + id);
        }
        bookRepository.deleteById(id);
        log.info("deleteByUUID: deleted id={}", id);
    }
}
