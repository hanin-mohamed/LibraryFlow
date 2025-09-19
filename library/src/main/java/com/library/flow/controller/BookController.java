package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.entity.Book;
import com.library.flow.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books")
public class BookController {

    private final BookService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "List books (paging & sorting only)")
    public AppResponse<Page<Book>> findAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort, "title");
        return AppResponse.ok(service.findAll(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Create book entity")
    public AppResponse<UUID> addBook(@RequestBody Book body, HttpServletResponse res) {
        UUID id = service.addBook(body);
        return AppResponse.created(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Update book")
    public AppResponse<Void> updateByUUID(@PathVariable UUID id, @RequestBody Book body) {
        service.updateBook(id, body);
        return AppResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete book")
    public AppResponse<Void> deleteByUUID(@PathVariable UUID id) {
        service.deleteByUUID(id);
        return AppResponse.ok(null);
    }

    private Pageable buildPageable(int page, int size, String sort, String defaultField) {
        String field = defaultField;
        String directionRaw = "asc";
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            field = parts.length > 0 && !parts[0].isBlank() ? parts[0] : defaultField;
            directionRaw = parts.length > 1 && !parts[1].isBlank() ? parts[1] : "asc";
        }
        Sort.Direction direction = Sort.Direction.fromString(directionRaw);
        return PageRequest.of(page, Math.min(size, 100), Sort.by(direction, field));
    }
}
