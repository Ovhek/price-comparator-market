package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.DiscountRepository;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link DiscountService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Discount saveOrUpdateDiscount(Product product, Store store,
                                         BigDecimal packageQuantity, UnitOfMeasure packageUnit,
                                         Integer percentage, LocalDate fromDate, LocalDate toDate,
                                         LocalDate recordedAtDate) {
        Assert.notNull(product, "Product cannot be null for Discount.");
        Assert.notNull(store, "Store cannot be null for Discount.");

        Optional<Discount> existingOpt = discountRepository.findByProductAndStoreAndFromDateAndPackageQuantityAndPackageUnit(
                product, store, fromDate, packageQuantity, packageUnit
        );

        Discount discountToSave;
        if (existingOpt.isPresent()) {
            discountToSave = existingOpt.get();
            // Only update if new record is more recent or details differ
            if (!discountToSave.getPercentage().equals(percentage) ||
                    !discountToSave.getToDate().equals(toDate) ||
                    recordedAtDate.isAfter(discountToSave.getRecordedAtDate()) ) {

                log.debug("Updating existing Discount for Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}. Old %: {}, New %: {}. Old ToDate: {}, New ToDate: {}",
                        product.getId(), store.getName(), fromDate, packageQuantity, packageUnit,
                        discountToSave.getPercentage(), percentage,
                        discountToSave.getToDate(), toDate);

                discountToSave.setPercentage(percentage);
                discountToSave.setToDate(toDate);
                discountToSave.setRecordedAtDate(recordedAtDate);
            } else {
                log.debug("Skipping update for existing Discount as new data is not different or newer. Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}",
                        product.getId(), store.getName(), fromDate, packageQuantity, packageUnit);
                return discountToSave; // Return existing if no update needed
            }
        } else {
            discountToSave = new Discount(
                    product,
                    store,
                    percentage,
                    fromDate,
                    toDate,
                    recordedAtDate,
                    packageQuantity,
                    packageUnit
            );
            log.debug("Creating new Discount for Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}, %: {}",
                    product.getId(), store.getName(), discountToSave.getFromDate(),
                    discountToSave.getPackageQuantity(), discountToSave.getPackageUnit(), discountToSave.getPercentage());
        }
        return discountRepository.save(discountToSave);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Discount> findDiscountByNaturalKey(Product product, Store store, LocalDate fromDate, BigDecimal packageQuantity, UnitOfMeasure packageUnit) {
        return discountRepository.findByProductAndStoreAndFromDateAndPackageQuantityAndPackageUnit(product, store, fromDate, packageQuantity, packageUnit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Discount> findActiveDiscountsByDate(LocalDate date, Pageable pageable) {
        Assert.notNull(date, "Date cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");
        return discountRepository.findActiveDiscountsByDate(date, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Discount> findActiveDiscountsByProductAndDate(Long productId, LocalDate date) {
        Assert.notNull(productId, "Product ID cannot be null.");
        Assert.notNull(date, "Date cannot be null.");
        return discountRepository.findActiveDiscountsByProductAndDate(productId, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Discount> findDiscountsRecordedAfter(LocalDate sinceDate, Pageable pageable) {
        Assert.notNull(sinceDate, "SinceDate cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");
        return discountRepository.findByRecordedAtDateAfter(sinceDate, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Discount> findTopActiveDiscounts(LocalDate date, Pageable pageable) {
        Assert.notNull(date, "Date cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");
        return discountRepository.findTopActiveDiscounts(date, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Discount save(Discount discount) {
        Assert.notNull(discount, "Discount to save must not be null.");
        log.debug("Saving Discount: {}", discount);
        return discountRepository.save(discount);
    }
}