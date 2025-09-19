package com.library.flow.service;

import com.library.flow.common.dto.BorrowStatus;
import com.library.flow.common.dto.CreateBorrowRequest;
import com.library.flow.common.error.custom.NotFoundException;
import com.library.flow.entity.Book;
import com.library.flow.entity.BorrowBook;
import com.library.flow.entity.Member;
import com.library.flow.repository.BookRepository;
import com.library.flow.repository.BorrowRepository;
import com.library.flow.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Value("${library.defaultLoanDays:14}")
    private int defaultLoanDays;

    @Value("${library.maxOpenLoansPerMember:5}")
    private int maxOpenLoansPerMember;

    private static final Set<BorrowStatus> ACTIVE_STATUSES = EnumSet.of(BorrowStatus.OPEN, BorrowStatus.OVERDUE);

    @Transactional
    public UUID borrow(CreateBorrowRequest request) {
        Instant nowUtc = Instant.now();
        Instant dueAtFinal = request.dueAt() != null ? request.dueAt() : nowUtc.plus(defaultLoanDays, ChronoUnit.DAYS);
        if (dueAtFinal.isBefore(nowUtc)) throw new IllegalArgumentException("dueAt must be in the future");

        Member member = memberRepository.
                findById(request.memberId()).orElseThrow(() -> new NotFoundException("member",request.memberId()));
        Book book = bookRepository.findByIdForUpdate(request.bookId());
        if (book == null) throw new NotFoundException("book", request.bookId());

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("no copies available for this book");
        }

        int openCountForMember = borrowRepository.countByMember_IdAndStatus(member.getId(), BorrowStatus.OPEN);
        if (openCountForMember >= maxOpenLoansPerMember) {
            throw new IllegalStateException("member reached max open borrows");
        }

        boolean alreadyBorrowedSameBook = borrowRepository.existsByMember_IdAndBook_IdAndStatusIn(
                member.getId(), book.getId(), ACTIVE_STATUSES
        );
        if (alreadyBorrowedSameBook) {
            throw new IllegalStateException("member already has an active borrow for this book");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BorrowBook borrowTransaction = BorrowBook.builder()
                .member(member)
                .book(book)
                .borrowedAt(nowUtc)
                .dueAt(dueAtFinal)
                .status(BorrowStatus.OPEN)
                .build();

        borrowRepository.save(borrowTransaction);
        log.info("Borrow created: transactionId={}, bookId={}, availableAfter={}", borrowTransaction.getId(), book.getId(), book.getAvailableCopies());
        return borrowTransaction.getId();
    }

    @Transactional
    public void returnBook(UUID transactionId, Instant requestedReturnedAt) {
        BorrowBook borrowTransaction = borrowRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("borrow_transaction"));

        if (BorrowStatus.RETURNED.equals(borrowTransaction.getStatus())) return;

        Instant returnedAtFinal = requestedReturnedAt != null ? requestedReturnedAt : Instant.now();
        if (borrowTransaction.getBorrowedAt() != null && returnedAtFinal.isBefore(borrowTransaction.getBorrowedAt())) {
            throw new IllegalArgumentException("returnedAt must be >= borrowedAt");
        }

        borrowTransaction.setReturnedAt(returnedAtFinal);
        borrowTransaction.setStatus(BorrowStatus.RETURNED);

        Book book = bookRepository.findByIdForUpdate(borrowTransaction.getBook().getId());
        if (book != null) {
            int totalCopies = book.getTotalCopies() == null ? 0 : book.getTotalCopies();
            int availableCopies = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
            int newAvailable = availableCopies + 1;
            book.setAvailableCopies(newAvailable > totalCopies ? totalCopies : newAvailable);
        }

        log.info("Borrow returned: transactionId={}", transactionId);
    }

    @Transactional
    public void markOverdue(UUID transactionId) {
        BorrowBook borrowTransaction = borrowRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("borrow_transaction"));

        if (!BorrowStatus.RETURNED.equals(borrowTransaction.getStatus())
                && borrowTransaction.getDueAt() != null
                && Instant.now().isAfter(borrowTransaction.getDueAt())) {
            borrowTransaction.setStatus(BorrowStatus.OVERDUE);
            log.warn("Borrow overdue: transactionId={}", transactionId);
        }
    }
}
