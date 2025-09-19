package com.library.flow.service;

import com.library.flow.common.dto.CreateBookRequest;
import com.library.flow.common.dto.UpdateBookRequest;
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
    public Book addBook(CreateBookRequest request) {
        log.info("addBook: start");

        Book book = new Book();
        book.setId(null);
        book.setCreatedAt(Instant.now());
        book.setTitle(request.title());
        book.setPublicationYear(request.publicationYear());
        book.setLanguage(request.language());
        book.setEdition(request.edition());
        book.setSummary(request.summary());
        book.setCoverImageUrl(request.coverImageUrl());

        if (request.publisherId() != null) {
            book.setPublisher(resolvePublisher(request.publisherId()));
        }

        if (request.authorIds() != null) {
            book.setAuthors(resolveAuthors(request.authorIds()));
        }

        if (request.categoryIds() != null) {
            book.setCategories(resolveCategories(request.categoryIds()));
        }

        book.setTotalCopies(request.totalCopies() != null ? request.totalCopies() : 0);
        book.setAvailableCopies(request.availableCopies() != null
                ? request.availableCopies()
                : book.getTotalCopies());

        bookRepository.save(book);
        log.info("addBook: saved id={}", book.getId());
        return book;
    }

    @Transactional
    public Book updateBook(UUID id, UpdateBookRequest request) {
        log.info("updateBook: id={}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("book"));

        if (request.title() != null) book.setTitle(request.title());
        if (request.publicationYear() != null) book.setPublicationYear(request.publicationYear());
        if (request.language() != null) book.setLanguage(request.language());
        if (request.edition() != null) book.setEdition(request.edition());
        if (request.summary() != null) book.setSummary(request.summary());
        if (request.coverImageUrl() != null) book.setCoverImageUrl(request.coverImageUrl());

        if (request.publisherId() != null) {
            book.setPublisher(resolvePublisher(request.publisherId()));
        } else if (request.publisherId() == null) {
            book.setPublisher(null);
        }

        if (request.authorIds() != null) {
            Set<Author> authors = resolveAuthors(request.authorIds());
            book.getAuthors().clear();
            if (!authors.isEmpty()) book.getAuthors().addAll(authors);
        }

        if (request.categoryIds() != null) {
            Set<Category> categories = resolveCategories(request.categoryIds());
            book.getCategories().clear();
            if (!categories.isEmpty()) book.getCategories().addAll(categories);
        }

        if (request.totalCopies() != null) book.setTotalCopies(request.totalCopies());
        if (request.availableCopies() != null) book.setAvailableCopies(request.availableCopies());

        log.info("updateBook: done id={}", id);
        return book;
    }

    public Page<Book> findAll(Pageable pageable) {
        log.debug("findAll: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable);
    }

    public void deleteByUUID(UUID id) {
        log.info("deleteByUUID: id={}", id);
        if (!bookRepository.existsById(id)) throw new EntityNotFoundException("book not found: " + id);
        bookRepository.deleteById(id);
        log.info("deleteByUUID: deleted id={}", id);
    }

    private Publisher resolvePublisher(UUID id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("publisher"));
    }

    private Set<Author> resolveAuthors(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptySet();
        return new HashSet<>(authorRepository.findAllById(ids));
    }

    private Set<Category> resolveCategories(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptySet();
        return new HashSet<>(categoryRepository.findAllById(ids));
    }
}
