package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.repository.StoreRepository;
import com.alexcruceat.pricecomparatormarket.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Implementation of {@link StoreService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Store findOrCreateStore(String name) {
        Assert.hasText(name, "Store name must not be null or blank.");
        String trimmedName = name.trim();
        return storeRepository.findByNameIgnoreCase(trimmedName)
                .orElseGet(() -> {
                    log.info("Store not found, creating new store: '{}'", trimmedName);
                    return storeRepository.save(new Store(trimmedName));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Store> findStoreById(Long id) {
        Assert.notNull(id, "Store id must not be null.");
        return storeRepository.findById(id);
    }
}
