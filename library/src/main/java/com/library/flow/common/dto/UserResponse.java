package com.library.flow.common.dto;

import com.library.flow.auth.model.Role;

import java.util.UUID;

public record UserResponse(UUID id, String fullName, String username, String email, Role role, boolean enabled) {}
