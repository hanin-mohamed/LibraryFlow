package com.library.flow.service;

import com.library.flow.common.dto.CreateAuthorRequest;
import com.library.flow.entity.Author;
import com.library.flow.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Transactional
    public UUID createAuthor(CreateAuthorRequest author){
        log.info("createAuthor: start");
        authorRepository.save(author);
        log.info("createAuthor: saved id={}", author.getId());
        return author.getId();
    }

    @Transactional
    public void updateAuthorInfo(UUID id, Author author){
        log.info("updateAuthorInfo: id={}", id);
        Author a = authorRepository.findById(id).orElseThrow(() -> {
            log.warn("updateAuthorInfo: author not found id={}", id);
            return new EntityNotFoundException("author");
        });
        log.debug("updateAuthorInfo: name -> {}", author.getName());
        a.setName(author.getName());
        log.info("updateAuthorInfo: done id={}", id);
    }

    public Page<Author> getBookAuthors(Pageable p){
        log.info("getBookAuthors: page={}, size={}", p.getPageNumber(), p.getPageSize());
        return authorRepository.findAll(p);
    }

    @Transactional
    public void delete(UUID id){
        log.info("deleteAuthor: id={}", id);
        authorRepository.deleteById(id);
        log.info("deleteAuthor: deleted id={}", id);
    }
}
