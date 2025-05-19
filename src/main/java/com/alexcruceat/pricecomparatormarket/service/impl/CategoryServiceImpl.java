package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.repository.CategoryRepository;
import com.alexcruceat.pricecomparatormarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Implementation of {@link CategoryService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Category findOrCreateCategory(String name) {
        Assert.hasText(name, "Category name must not be null or blank.");
        String trimmedName = name.trim();
        return categoryRepository.findByNameIgnoreCase(trimmedName)
                .orElseGet(() -> {
                    log.info("Category not found, creating new category: '{}'", trimmedName);
                    return categoryRepository.save(new Category(trimmedName));
                });
    }
}
