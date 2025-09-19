package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.entity.Author;

import com.library.flow.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Authors")
public class AuthorController {

    private final AuthorService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "List authors (paging & sorting)")
    public AppResponse<Page<Author>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort, "name");
        return AppResponse.ok(service.getBookAuthors(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Create author (entity body)")
    public AppResponse<UUID> addAuthor(@RequestBody Author body ) {
        UUID id = service.createAuthor(body);
        return AppResponse.created(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Update author (entity body)")
    public AppResponse<Void> updateAuthorInfo(@PathVariable UUID id, @RequestBody Author body) {
        service.updateAuthorInfo(id, body);
        return AppResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete author")
    public AppResponse<Void> deleteById(@PathVariable UUID id) {
        service.delete(id);
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
