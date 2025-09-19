package com.library.flow.service;

import com.library.flow.entity.Category;
import com.library.flow.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository repository;

    @Transactional
    public UUID createCategory(Category category) {
        log.info("createCategory: start");
        category.setId(null);
        if (category.getParent() != null) {
            UUID parentId = category.getParent().getId();
            log.debug("createCategory: parentId={}", parentId);
            category.setParent(parentId != null
                    ? repository.findById(parentId).orElseThrow(() -> {
                log.warn("createCategory: parent not found id={}", parentId);
                return new EntityNotFoundException("parent");
            })
                    : null);
        }
        repository.save(category);
        log.info("createCategory: saved id={}", category.getId());
        return category.getId();
    }

    @Transactional
    public void updateCategory(UUID id, Category changes) {
        log.info("updateCategory: id={}", id);
        Category category = repository.findById(id).orElseThrow(() -> {
            log.warn("updateCategory: category not found id={}", id);
            return new EntityNotFoundException("category");
        });
        log.debug("updateCategory: name -> {}", changes.getName());
        category.setName(changes.getName());

        if (changes.getParent() != null) {
            UUID parentId = changes.getParent().getId();
            log.debug("updateCategory: requested parentId={}", parentId);
            if (parentId != null && id.equals(parentId)) {
                log.warn("updateCategory: category cannot be its own parent id={}", id);
                throw new IllegalArgumentException("category cannot be its own parent");
            }
            category.setParent(parentId != null
                    ? repository.findById(parentId).orElseThrow(() -> {
                log.warn("updateCategory: parent not found id={}", parentId);
                return new EntityNotFoundException("parent");
            })
                    : null);
        } else {
            log.debug("updateCategory: clearing parent");
            category.setParent(null);
        }
        log.info("updateCategory: done id={}", id);
    }

    public Page<Category> getAllCategories(Pageable pageable) {
        log.info("getAllCategories: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    public List<Category> getCategTree() {
        log.info("getCategTree: start building tree");
        List<Category> all = repository.findAll();
        log.debug("getCategTree: fetched {} categories", all.size());
        Map<UUID, Category> map = new LinkedHashMap<>();
        for (Category c : all) {
            c.setChildren(new ArrayList<>());
            map.put(c.getId(), c);
        }
        List<Category> roots = new ArrayList<>();
        for (Category category : map.values()) {
            Category parent = category.getParent();
            if (parent == null) {
                roots.add(category);
            } else {
                Category p = map.get(parent.getId());
                if (p != null) {
                    category.setParent(p);
                    p.getChildren().add(category);
                } else {
                    log.debug("getCategTree: missing parent in map id={}, pushing as root", parent.getId());
                    roots.add(category);
                }
            }
        }
        log.info("getCategTree: built tree with {} roots", roots.size());
        return roots;
    }

    @Transactional
    public void deleteById(UUID id) {
        log.info("deleteCategoryById: id={}", id);
        if (repository.existsByParent_Id(id)) {
            log.warn("deleteCategoryById: has children id={}", id);
            throw new IllegalStateException("cannot delete category with children");
        }
        repository.deleteById(id);
        log.info("deleteCategoryById: deleted id={}", id);
    }
}
