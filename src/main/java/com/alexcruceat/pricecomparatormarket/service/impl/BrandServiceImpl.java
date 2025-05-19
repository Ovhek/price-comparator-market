package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.repository.BrandRepository;
import com.alexcruceat.pricecomparatormarket.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Implementation of {@link BrandService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Brand findOrCreateBrand(String name) {
        Assert.hasText(name, "Brand name must not be null or blank.");
        String trimmedName = name.trim();
        return brandRepository.findByNameIgnoreCase(trimmedName)
                .orElseGet(() -> {
                    log.info("Brand not found, creating new brand: '{}'", trimmedName);
                    return brandRepository.save(new Brand(trimmedName));
                });
    }
}
