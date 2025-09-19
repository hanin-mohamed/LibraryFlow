package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.common.dto.CreateBookRequest;
import com.library.flow.common.dto.UpdateBookRequest;
import com.library.flow.entity.Book;
import com.library.flow.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    public AppResponse<Book> addBook(@Valid @RequestBody CreateBookRequest body) {
        Book book = service.addBook(body);
        return AppResponse.created(book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public AppResponse<Book> updateBook(@PathVariable UUID id,
                                        @Valid @RequestBody UpdateBookRequest body) {
        Book updated = service.updateBook(id, body);
        return AppResponse.ok(updated);
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
