package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import com.alexcruceat.pricecomparatormarket.repository.PriceEntryRepository;
import com.alexcruceat.pricecomparatormarket.service.PriceEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PriceEntryService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEntryServiceImpl implements PriceEntryService {

    private final PriceEntryRepository priceEntryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PriceEntry saveOrUpdatePriceEntry(Product product, Store store, LocalDate entryDate,
                                             String storeProductId, BigDecimal price, String currency,
                                             BigDecimal packageQuantity, UnitOfMeasure unit) {
        Assert.notNull(product, "Product cannot be null for PriceEntry.");
        Assert.notNull(store, "Store cannot be null for PriceEntry.");
        Assert.notNull(entryDate, "EntryDate cannot be null for PriceEntry.");
        Assert.notNull(price, "Price cannot be null for PriceEntry.");
        Assert.hasText(currency, "Currency must be provided for PriceEntry.");
        Assert.notNull(packageQuantity, "PackageQuantity cannot be null for PriceEntry.");
        Assert.notNull(unit, "UnitOfMeasure cannot be null for PriceEntry.");

        Optional<PriceEntry> existingOpt = priceEntryRepository.findByProductAndStoreAndEntryDate(product, store, entryDate);

        PriceEntry priceEntryToSave;
        if (existingOpt.isPresent()) {
            priceEntryToSave = existingOpt.get();
            log.debug("Updating existing PriceEntry for Product ID: {}, Store: {}, Date: {}. Old Price: {}, New Price: {}",
                    product.getId(), store.getName(), entryDate, priceEntryToSave.getPrice(), price);
            priceEntryToSave.setPrice(price);
            priceEntryToSave.setCurrency(currency);
            priceEntryToSave.setPackageQuantity(packageQuantity);
            priceEntryToSave.setPackageUnit(unit);
            priceEntryToSave.setStoreProductId(storeProductId); // Update store's product ID if it changed
        } else {
            priceEntryToSave = new PriceEntry(
                    product, store, storeProductId, price, currency,
                    packageQuantity, unit, entryDate
            );
            log.debug("Creating new PriceEntry for Product ID: {}, Store: {}, Date: {}, Price: {}",
                    product.getId(), store.getName(), entryDate, priceEntryToSave.getPrice());
        }
        return priceEntryRepository.save(priceEntryToSave);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<PriceEntry> findByProductAndStoreAndEntryDate(Product product, Store store, LocalDate date) {
        Assert.notNull(product, "Product cannot be null.");
        Assert.notNull(store, "Store cannot be null.");
        Assert.notNull(date, "Date cannot be null.");
        return priceEntryRepository.findByProductAndStoreAndEntryDate(product, store, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<PriceEntry> findByProductOrderByEntryDateDesc(Product product) {
        Assert.notNull(product, "Product cannot be null.");
        return priceEntryRepository.findByProductOrderByEntryDateDesc(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriceEntry save(PriceEntry newPriceEntry) {
        Assert.notNull(newPriceEntry, "Product to save must not be null.");
        log.debug("Saving Price Entry: {}", newPriceEntry);
        return priceEntryRepository.save(newPriceEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<PriceEntry> getPriceEntriesForProductAndStore(Long productId, Long storeId, LocalDate startDate, LocalDate endDate) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(storeId, "Store ID cannot be null.");
        Assert.notNull(startDate, "Start date cannot be null.");
        Assert.notNull(endDate, "End date cannot be null.");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date.");
        return priceEntryRepository.findByProductIdAndStoreIdAndEntryDateBetweenOrderByEntryDateAsc(productId, storeId, startDate, endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<PriceEntry> getPriceEntriesForProduct(Long productId, LocalDate startDate, LocalDate endDate) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(startDate, "Start date cannot be null.");
        Assert.notNull(endDate, "End date cannot be null.");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date.");
        return priceEntryRepository.findByProductIdAndEntryDateBetweenOrderByEntryDateAsc(productId, startDate, endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<PriceEntry> findFirstByProduct_IdAndStore_IdAndEntryDateLessThanEqualOrderByEntryDateDesc(Long productId, Long storeId, LocalDate referenceDate) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(storeId, "Store ID cannot be null.");
        Assert.notNull(referenceDate, "Reference date cannot be null.");
        return priceEntryRepository.findFirstByProduct_IdAndStore_IdAndEntryDateLessThanEqualOrderByEntryDateDesc(
                productId, storeId, referenceDate
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<PriceEntry> findByProductIdAndEntryDate(Long productId, LocalDate entryDate) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(entryDate, "Entry date cannot be null.");
        return priceEntryRepository.findByProductIdAndEntryDate(productId, entryDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<PriceEntry> findLatestPriceEntriesPerStoreForProduct(Long productId, LocalDate referenceDate) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(referenceDate, "Reference date cannot be null.");

        List<PriceEntry> allEntries = priceEntryRepository.findLatestPriceEntriesPerStoreForProduct(productId, referenceDate);

        // We need to pick the first entry for each store.
        return allEntries.stream()
                .collect(Collectors.groupingBy(
                        pe -> pe.getStore().getId(),
                        Collectors.maxBy(Comparator.comparing(PriceEntry::getEntryDate))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<PriceEntry> findFirstByProductAndEntryDateLessThanEqualOrderByEntryDateDesc(Product product, LocalDate referenceDate) {
        Assert.notNull(product, "Product cannot be null.");
        Assert.notNull(referenceDate, "Reference date cannot be null.");
        return priceEntryRepository.findFirstByProductAndEntryDateLessThanEqualOrderByEntryDateDesc(product, referenceDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PriceEntry> findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(Product product, Store store, BigDecimal packageQuantity, UnitOfMeasure packageUnit, LocalDate referenceDate) {
        Assert.notNull(product, "Product for discount cannot be null.");
        Assert.notNull(store, "Store for discount cannot be null.");
        Assert.notNull(packageQuantity, "Discount package quantity cannot be null.");
        Assert.notNull(packageUnit, "Discount package unit cannot be null.");

        return priceEntryRepository
                .findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(
                        product, store, packageQuantity, packageUnit, referenceDate);
    }
}
