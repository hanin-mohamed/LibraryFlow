package com.library.flow.controller;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrowing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Borrowing")
public class BorrowController {

    private final BorrowService service;

    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "Borrow a book (memberId, bookId, optional dueAt)")
    public AppResponse<UUID> borrow(@RequestParam UUID memberId,
                                    @RequestParam UUID bookId,
                                    @RequestParam(required = false) Instant dueAt) {
        UUID id = service.borrow(memberId, bookId, dueAt);
        return AppResponse.created(id);
    }

    @PostMapping("/{txId}/return")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','STAFF')")
    @Operation(summary = "Return a borrowed book (optional returnedAt)")
    public AppResponse<Void> returnBook(@PathVariable UUID txId,
                                        @RequestParam(required = false) Instant returnedAt) {
        service.returnBook(txId, returnedAt);
        return AppResponse.ok(null);
    }

    @PostMapping("/{txId}/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark transaction as overdue")
    public AppResponse<Void> markOverdue(@PathVariable UUID txId) {
        service.markOverdue(txId);
        return AppResponse.ok(null);
    }
}
