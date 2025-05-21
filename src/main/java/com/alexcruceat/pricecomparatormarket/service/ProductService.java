package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.dto.ProductValueDTO;
import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for managing {@link Product} entities.
 * Encapsulates business logic related to products, including finding, creating,
 * and potentially updating them.
 */
public interface ProductService {

    /**
     * Finds an existing {@link Product} by its name and {@link Brand} (case-insensitive for name)
     * or creates a new one if not found. If the product is found but its category differs
     * from the provided category, the product's category will be updated.
     *
     * @param name     The name of the product. Must not be null or blank.
     * @param brand    The {@link Brand} of the product. Must not be null.
     * @param category The {@link Category} for the product. Used for creation or updating the product's category. Must not be null.
     * @return The existing or newly created (and potentially updated) {@link Product} entity.
     * @throws IllegalArgumentException if name, brand, or category is null or invalid.
     */
    Product findOrCreateProductAndUpdateCategory(String name, Brand brand, Category category);

    /**
     * Finds a product by its unique ID.
     *
     * @param id The ID of the product.
     * @return An {@link Optional} containing the product if found, or empty otherwise.
     */
    Optional<Product> findById(Long id);

    /**
     * Finds a product by its name and brand.
     *
     * @param name  The name of the product (case-insensitive).
     * @param brand The brand of the product.
     * @return An {@link Optional} containing the product if found.
     */
    Optional<Product> findByNameIgnoreCaseAndBrand(String name, Brand brand);

    Product save(Product newProduct);

    /**
     * Finds all products matching the given specification, with pagination and sorting.
     *
     * @param spec     A {@link Specification} to filter products (can be null for no filtering).
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of {@link Product} entities.
     */
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    /**
     * Finds all products, with pagination and sorting.
     *
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of {@link Product} entities.
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete.
     */
    void deleteById(Long id);

    /**
     * Finds products matching the given criteria and enriches them with value-per-unit information.
     * This method will look at the most recent price entry for each product in each store (or a specific store).
     *
     * @param name       Optional product name filter.
     * @param categoryId Optional category ID filter.
     * @param brandId    Optional brand ID filter.
     * @param storeIdOpt Optional store ID filter. If present, only considers prices from this store.
     *                   If absent, might consider best price across stores or latest from any store (needs definition).
     *                   For simplicity, let's assume if storeId is absent, it processes products and their latest prices
     *                   from any store they are found in.
     * @param referenceDate The date to consider for "current" prices.
     * @param pageable   Pagination and sorting information.
     * @return A {@link Page} of {@link ProductValueDTO}s.
     */
    Page<ProductValueDTO> findProductsWithValueAnalysis(
            String name, Long categoryId, Long brandId, Optional<Long> storeIdOpt,
            LocalDate referenceDate, Pageable pageable
    );

}
