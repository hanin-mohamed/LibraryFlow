package com.library.flow.repository;

import com.library.flow.common.dto.BorrowStatus;
import com.library.flow.entity.BorrowBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface BorrowRepository extends JpaRepository<BorrowBook, UUID> {

    int countByMember_IdAndStatus(UUID memberId, BorrowStatus status);
    boolean existsByMember_IdAndBook_IdAndStatusIn(UUID memberId, UUID bookId, Set<BorrowStatus> statuses);

}
