package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.dto.DiscountedProductDTO;
import com.alexcruceat.pricecomparatormarket.mapper.ProductMapper;
import com.alexcruceat.pricecomparatormarket.mapper.StoreMapper;
import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.DiscountRepository;
import com.alexcruceat.pricecomparatormarket.repository.PriceEntryRepository;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final PriceEntryRepository priceEntryRepository;

    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;


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
            // Rule 1: If incoming record's recordedAtDate is strictly older, do not update.
            if (recordedAtDate.isBefore(discountToSave.getRecordedAtDate())) {
                log.debug("Skipping update for existing Discount: incoming recordedAtDate ({}) is older than existing ({}). Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}",
                        recordedAtDate, discountToSave.getRecordedAtDate(), product.getId(), store.getName(), fromDate, packageQuantity, packageUnit);
                return discountToSave; // Return existing, do not save
            }

            // Rule 2: If recordedAtDate is the same, only update if other critical fields have changed.
            // If all relevant fields are identical, no need to save.
            boolean detailsChanged = !discountToSave.getPercentage().equals(percentage) ||
                    !discountToSave.getToDate().equals(toDate);

            if (recordedAtDate.equals(discountToSave.getRecordedAtDate()) && !detailsChanged) {
                log.debug("Skipping update for existing Discount: incoming recordedAtDate is same and other details are identical. Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}",
                        product.getId(), store.getName(), fromDate, packageQuantity, packageUnit);
                return discountToSave; // Data is identical for this date, return existing, do not save
            }


            log.debug("Updating existing Discount for Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}. Old %: {}, New %: {}. Old ToDate: {}, New ToDate: {}",
                        product.getId(), store.getName(), fromDate, packageQuantity, packageUnit,
                        discountToSave.getPercentage(), percentage,
                        discountToSave.getToDate(), toDate);

            discountToSave.setPercentage(percentage);
            discountToSave.setToDate(toDate);
            discountToSave.setRecordedAtDate(recordedAtDate);
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
    @Transactional(readOnly = true) // This is a read operation
    public Page<DiscountedProductDTO> findBestActiveDiscounts(LocalDate referenceDate, Pageable pageable) {
        Assert.notNull(referenceDate, "Reference date cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");

        // 1. Fetch all discounts active on the referenceDate, sorted by percentage.
        // The repository method findTopActiveDiscounts already sorts by percentage.
        // We fetch all active discounts first, then apply pagination after enrichment.
        // This is because original price fetching might filter out some discounts if price is not found.
        // For large number of active discounts, this could be memory intensive.
        // A more optimized way would be a complex DB query or iterative fetching.

        Pageable unbounded;

        if(pageable.getSort().isSorted()){
            unbounded = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort().and(Sort.by(Sort.Direction.DESC, "percentage")));
        } else {
            unbounded = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "percentage"));
        }

        Page<Discount> activeDiscountsPage = discountRepository.findActiveDiscountsByDate(referenceDate, unbounded);
        List<Discount> activeDiscounts = activeDiscountsPage.getContent();

        List<DiscountedProductDTO> discountedProductDTOs = new ArrayList<>();

        for (Discount discount : activeDiscounts) {
            // 2. For each discount, find the most recent PriceEntry for that product/store/package
            // on or before the discount's fromDate (or referenceDate, depending on definition of "original price").
            // Let's assume "original price" is the latest available price for that item at that store.
            // We need to match on product, store, packageQuantity, and packageUnit.
            Optional<PriceEntry> priceEntryOpt = priceEntryRepository
                    .findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(
                            discount.getProduct(),
                            discount.getStore(),
                            discount.getPackageQuantity(),
                            discount.getPackageUnit(),
                            referenceDate
                    );

            if (priceEntryOpt.isPresent()) {
                PriceEntry originalPriceEntry = priceEntryOpt.get();
                BigDecimal originalPrice = originalPriceEntry.getPrice();
                BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(discount.getPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
                BigDecimal discountedPrice = originalPrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);

                discountedProductDTOs.add(DiscountedProductDTO.builder()
                        .product(productMapper.toDTO(discount.getProduct()))
                        .store(storeMapper.toDTO(discount.getStore()))
                        .discountPercentage(discount.getPercentage())
                        .originalPrice(originalPrice)
                        .discountedPrice(discountedPrice)
                        .packageQuantity(discount.getPackageQuantity())
                        .packageUnit(discount.getPackageUnit())
                        .discountEndDate(discount.getToDate())
                        .build());
            } else {
                log.warn("Could not find a suitable original price entry for active discount ID: {} (Product: {}, Store: {}). Skipping this discount from 'best discounts' list.",
                        discount.getId(), discount.getProduct().getName(), discount.getStore().getName());
            }
        }

        // Manual pagination after enrichment and potential filtering
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), discountedProductDTOs.size());
        List<DiscountedProductDTO> pageContent = List.of();
        if (start <= end) {
            pageContent = discountedProductDTOs.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, discountedProductDTOs.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DiscountedProductDTO> findNewlyAddedDiscounts(LocalDate sinceDate, LocalDate referenceDateForPrices, Pageable pageable) {
        Assert.notNull(sinceDate, "SinceDate cannot be null for finding new discounts.");
        Assert.notNull(referenceDateForPrices, "ReferenceDateForPrices cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");

        // 1. Fetch discounts recorded on or after 'sinceDate'.
        // We might want to ensure a default sort by recordedAtDate descending if not specified by client.
        PageRequest queryPageable;
        if (pageable.getSort().isSorted()) {
            // Use the client-provided sort
            queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        } else {
            // Default sort for new discounts: most recent first, then by product name
            queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "recordedAtDate")
                            .and(Sort.by(Sort.Direction.ASC, "product.name")));
        }


        Page<Discount> newDiscountsPage = discountRepository.findByRecordedAtDateAfter(sinceDate.minusDays(1), queryPageable);
        List<DiscountedProductDTO> enrichedNewDiscountDTOs = new ArrayList<>();

        for (Discount discount : newDiscountsPage.getContent()) {
            // 2. For each new discount, find its original price to calculate the discounted price.
            // Use referenceDateForPrices to get the relevant original price.
            Optional<PriceEntry> priceEntryOpt = priceEntryRepository
                    .findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(
                            discount.getProduct(),
                            discount.getStore(),
                            discount.getPackageQuantity(),
                            discount.getPackageUnit(),
                            referenceDateForPrices
                    );

            if (priceEntryOpt.isPresent()) {
                PriceEntry originalPriceEntry = priceEntryOpt.get();
                BigDecimal originalPrice = originalPriceEntry.getPrice();
                BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                        BigDecimal.valueOf(discount.getPercentage()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                );
                BigDecimal discountedPrice = originalPrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);

                enrichedNewDiscountDTOs.add(DiscountedProductDTO.builder()
                        .product(productMapper.toDTO(discount.getProduct()))
                        .store(storeMapper.toDTO(discount.getStore()))
                        .discountPercentage(discount.getPercentage())
                        .originalPrice(originalPrice)
                        .discountedPrice(discountedPrice)
                        .packageQuantity(discount.getPackageQuantity())
                        .packageUnit(discount.getPackageUnit())
                        .discountEndDate(discount.getToDate()) // Also include when the discount ends
                        .build());
            } else {
                log.warn("Could not find an original price entry for newly added discount ID: {} (Product: {}, Store: {} using reference date {}). Skipping this discount from 'new discounts' list.",
                        discount.getId(), discount.getProduct().getName(), discount.getStore().getName(), referenceDateForPrices);
            }
        }
        // Return a new Page with the enriched DTOs and original pagination info from newDiscountsPage
        return new PageImpl<>(enrichedNewDiscountDTOs, pageable, newDiscountsPage.getTotalElements());
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