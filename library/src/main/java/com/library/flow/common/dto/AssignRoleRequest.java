package com.library.flow.common.dto;

import com.library.flow.auth.model.Role;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(@NotNull Role role) {}
