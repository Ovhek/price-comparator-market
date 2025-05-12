package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Brand} entities.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Finds a brand by its unique name, ignoring case.
     *
     * @param name The name of the brand to find.
     * @return An {@link Optional} containing the found brand, or empty if not found.
     */
    Optional<Brand> findByNameIgnoreCase(String name);
}