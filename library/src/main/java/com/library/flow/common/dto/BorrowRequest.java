package com.library.flow.common.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BorrowRequest( @NotNull UUID memberId,
                             @NotNull UUID copyId,
                             Instant dueAt
) { }