package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.model.Category;

import java.util.Optional;

/**
 * Service interface for managing {@link Category} entities.
 */
public interface CategoryService {
    /**
     * Finds an existing {@link Category} by its name (case-insensitive) or creates a new one if not found.
     *
     * @param name The name of the category. Must not be null or blank.
     * @return The existing or newly created {@link Category} entity.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    Category findOrCreateCategory(String name);

    /**
     * Tries to find a Category by id
     * @param categoryId category ID
     * @return Optional Category
     */
    Optional<Category> findById(Long categoryId);
}
