package com.library.flow.common.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateBorrowRequest(
        @NotNull UUID memberId,
        @NotNull UUID copyId,
        @NotNull @Future(message = "dueAt must be in the future") Instant dueAt
) { }