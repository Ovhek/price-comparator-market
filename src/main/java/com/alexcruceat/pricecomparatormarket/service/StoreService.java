package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.model.Store;

import java.util.Optional;

/**
 * Service interface for managing {@link Store} entities.
 * Provides operations related to finding or creating stores.
 */
public interface StoreService {
    /**
     * Finds an existing {@link Store} by its name (case-insensitive) or creates a new one if not found.
     *
     * @param name The name of the store. Must not be null or blank.
     * @return The existing or newly created {@link Store} entity.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    Store findOrCreateStore(String name);


    /**
     * Finds an existing {@link Store} by its id or empty Optional.
     *
     * @param id The id of the store. Must not be null or blank.
     * @return The existing {@link Store} entity.
     * @throws IllegalArgumentException if the name is null or blank.
     */
    Optional<Store> findStoreById(Long id);
}