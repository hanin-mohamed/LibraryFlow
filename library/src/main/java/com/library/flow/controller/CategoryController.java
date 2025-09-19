package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.entity.Category;
import com.library.flow.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "List categories (paging & sorting)")
    public AppResponse<Page<Category>> getAllPublishers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort, "name");
        return AppResponse.ok(service.getAllCategories(pageable));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "Get category tree (roots with children)")
    public AppResponse<List<Category>> getTree() {
        return AppResponse.ok(service.getCategTree());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Create category (entity body)")
    public AppResponse<UUID> createPublisher(@RequestBody Category body, HttpServletResponse res) {
        UUID id = service.createCategory(body);
        return AppResponse.created(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Operation(summary = "Update category (entity body)")
    public AppResponse<Void> updatePublisher(@PathVariable UUID id, @RequestBody Category body) {
        service.updateCategory(id, body);
        return AppResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category (fails if it has children)")
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
