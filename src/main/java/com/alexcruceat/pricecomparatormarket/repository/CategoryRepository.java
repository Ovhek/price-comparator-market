package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its unique name, ignoring case.
     *
     * @param name The name of the category to find.
     * @return An {@link Optional} containing the found category, or empty if not found.
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
