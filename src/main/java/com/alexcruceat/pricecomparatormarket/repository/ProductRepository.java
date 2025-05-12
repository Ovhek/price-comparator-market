package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // For dynamic criteria queries later
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * Includes {@link JpaSpecificationExecutor} to enable criteria-based searching/filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Finds a product by its unique combination of name and brand, ignoring case for the name.
     * Note: Brand comparison is exact based on the Brand entity object.
     *
     * @param name The name of the product (case-insensitive).
     * @param brand The {@link Brand} entity associated with the product.
     * @return An {@link Optional} containing the found product, or empty if not found.
     */
    Optional<Product> findByNameIgnoreCaseAndBrand(String name, Brand brand);

}
