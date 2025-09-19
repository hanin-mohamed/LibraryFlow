package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.entity.Publisher;
import com.library.flow.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
@Tag(name = "Publishers")
public class PublisherController {

    private final PublisherService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "List publishers (paging & sorting)")
    public AppResponse<Page<Publisher>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort, "name");
        return AppResponse.ok(service.getPublishers(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Create publisher (entity body)")
    public AppResponse<UUID> create(@RequestBody Publisher body, HttpServletResponse res) {
        UUID id = service.addPublisher(body);
        res.setStatus(201);
        return AppResponse.created(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Update publisher (entity body)")
    public AppResponse<Void> update(@PathVariable UUID id, @RequestBody Publisher body) {
        service.updatePublisher(id, body);
        return AppResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete publisher")
    public AppResponse<Void> deleteById(@PathVariable UUID id) {
        service.deleteById(id);
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
