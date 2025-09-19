package com.library.flow.service;

import com.library.flow.entity.Publisher;
import com.library.flow.repository.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherService {

    private final PublisherRepository repository;

    @Transactional
    public UUID addPublisher(Publisher publisher) {
        log.info("addPublisher: start");
        publisher.setId(null);
        repository.save(publisher);
        log.info("addPublisher: saved id={}", publisher.getId());
        return publisher.getId();
    }

    @Transactional
    public void updatePublisher(UUID id, Publisher changes) {
        log.info("updatePublisher: id={}", id);
        Publisher publisher = repository.findById(id).orElseThrow(() -> {
            log.warn("updatePublisher: publisher not found id={}", id);
            return new EntityNotFoundException("publisher");
        });
        log.debug("updatePublisher: name -> {}", changes.getName());
        publisher.setName(changes.getName());
        log.info("updatePublisher: done id={}", id);
    }

    public Page<Publisher> getPublishers(Pageable pageable) {
        log.info("getPublishers: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional
    public void deleteById(UUID id) {
        log.info("deletePublisherById: id={}", id);
        repository.deleteById(id);
        log.info("deletePublisherById: deleted id={}", id);
    }
}
