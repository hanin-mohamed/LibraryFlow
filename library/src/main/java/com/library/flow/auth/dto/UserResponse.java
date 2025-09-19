package com.library.flow.auth.dto;

public record UserResponse(
        String id,
        String username,
        String fullName,
        String email,
        String role
) {}
