package com.library.flow.service;

import com.library.flow.common.error.custom.NotFoundException;
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
        if (category.getParent() != null && category.getParent().getId() != null) {
            UUID parentId = category.getParent().getId();
            if (parentId.equals(category.getId())) {
                throw new IllegalArgumentException("category cannot be its own parent");
            }
            category.setParent(repository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("parent", parentId)));
        } else {
            category.setParent(null);
        }
        repository.save(category);
        log.info("createCategory: saved id={}", category.getId());
        return category.getId();
    }

    @Transactional
    public void updateCategory(UUID id, Category changes) {
        log.info("updateCategory: id={}", id);
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("category"));
        category.setName(changes.getName());
        if (changes.getParent() != null && changes.getParent().getId() != null) {
            UUID parentId = changes.getParent().getId();
            if (id.equals(parentId)) {
                throw new IllegalArgumentException("category cannot be its own parent");
            }
            category.setParent(repository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("parent",parentId)));
        } else {
            category.setParent(null);
        }
    }

    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategTree() {
        List<Category> all = repository.findAll();
        Map<UUID, Category> map = new LinkedHashMap<>();
        for (Category c : all) {
            c.setChildren(new ArrayList<>());
            map.put(c.getId(), c);
        }
        List<Category> roots = new ArrayList<>();
        for (Category c : map.values()) {
            Category parent = c.getParent();
            if (parent == null) {
                roots.add(c);
            } else {
                Category p = map.get(parent.getId());
                if (p != null) {
                    c.setParent(p);
                    p.getChildren().add(c);
                } else {
                    roots.add(c);
                }
            }
        }
        return roots;
    }

    @Transactional
    public void deleteById(UUID id) {
        if (repository.existsByParent_Id(id)) {
            throw new IllegalStateException("cannot delete category with children");
        }
        repository.deleteById(id);
    }
}
