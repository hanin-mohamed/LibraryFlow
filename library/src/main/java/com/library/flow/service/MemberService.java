package com.library.flow.service;

import com.library.flow.common.error.custom.NotFoundException;
import com.library.flow.entity.Member;
import com.library.flow.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository repository;

    @Transactional
    public UUID addMember(Member member) {
        log.info("addMember: start");
        member.setId(null);
        if (member.getCreatedAt() == null) {
            member.setCreatedAt(Instant.now());
            log.debug("addMember: createdAt set to now");
        }
        repository.save(member);
        log.info("addMember: saved member id={}", member.getId());
        return member.getId();
    }

    @Transactional
    public void updateMemberById(UUID id, Member updated) {
        log.info("updateMemberById: id={}", id);
        Member member = repository.findById(id).orElseThrow(() -> {
            log.warn("updateMemberById: member not found id={}", id);
            return new NotFoundException("member",id);
        });

        if (updated.getFullName() != null) {
            log.debug("updateMemberById: updating fullName -> {}", updated.getFullName());
            member.setFullName(updated.getFullName());
        }
        if (updated.getEmail() != null) {
            log.debug("updateMemberById: updating email -> {}", updated.getEmail());
            member.setEmail(updated.getEmail());
        }
        if (updated.getPhone() != null) {
            log.debug("updateMemberById: updating phone -> {}", updated.getPhone());
            member.setPhone(updated.getPhone());
        }
        log.info("updateMemberById: done id={}", id);
    }

    public Page<Member> getAllMembers(Pageable pageable) {
        log.info("getAllMembers: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional
    public void deleteById(UUID id) {
        log.info("deleteMemberById: id={}", id);
        repository.deleteById(id);
        log.info("deleteMemberById: deleted id={}", id);
    }
}
