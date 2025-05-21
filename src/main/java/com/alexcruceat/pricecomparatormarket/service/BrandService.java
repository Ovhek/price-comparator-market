package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import java.util.Optional;

/**
 * Service interface for managing {@link Brand} entities.
 */
public interface BrandService {
    /**
     * Finds an existing {@link Brand} by its name (case-insensitive) or creates a new one if not found.
     *
     * @param name The name of the brand. Must not be null or blank.
     * @return The existing or newly created {@link Brand} entity.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    Brand findOrCreateBrand(String name);


    /**
     * Tries to get a Brand by ID
     * @param brandId brand ID.
     * @return an Optional of Brand.
     */
    Optional<Brand> findById(Long brandId);
}
