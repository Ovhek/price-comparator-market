package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Store} entities.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Finds a store by its unique name, ignoring case.
     *
     * @param name The name of the store to find.
     * @return An {@link Optional} containing the found store, or empty if not found.
     */
    Optional<Store> findByNameIgnoreCase(String name);
}
