package com.library.flow.auth.controller;

import com.library.flow.auth.dto.*;
import com.library.flow.auth.service.UserAdminService;
import com.library.flow.common.dto.AppResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List system users (ADMIN)")
    public AppResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username,asc") String sort
    ) {
        Pageable p = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]));
        return AppResponse.ok(service.getAllUsers(p));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return AppResponse.ok(service.getUserById(id));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create system user (ADMIN)")
    public AppResponse<UUID> createUser(@Valid @RequestBody CreateUserDTO dto) {
        UUID id = service.createUser(dto);
        return AppResponse.created(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update system user (ADMIN)")
    public AppResponse<Void> updateUserInfo(@PathVariable UUID id, @RequestBody UpdateUserDTO dto) {
        service.updateUser(id, dto);
        return AppResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete system user (ADMIN)")
    public AppResponse<Void> deleteUser(@PathVariable UUID id) {
        service.deleteById(id);
        return AppResponse.ok(null);
    }
}
