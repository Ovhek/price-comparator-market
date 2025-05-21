package com.alexcruceat.pricecomparatormarket.service;


import com.alexcruceat.pricecomparatormarket.dto.DiscountedProductDTO;
import com.alexcruceat.pricecomparatormarket.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Discount} entities.
 */
public interface DiscountService {

    /**
     * Saves or updates a discount for a given product, store, period, and package details.
     * If a discount exists for the specified combination (product, store, fromDate, package), it's updated.
     * Otherwise, a new one is created.
     *
     * @param product         The product for the discount.
     * @param store           The store offering the discount.
     * @param packageQuantity The package quantity for this discount.
     * @param packageUnit     The package unit for this discount.
     * @param percentage      The discount percentage.
     * @param fromDate        The start date of the discount.
     * @param toDate          The end date of the discount.
     * @param recordedAtDate  The date this discount information was recorded.
     * @return The saved or updated {@link Discount}.
     */
    Discount saveOrUpdateDiscount(Product product, Store store,
                                  BigDecimal packageQuantity, UnitOfMeasure packageUnit,
                                  Integer percentage, LocalDate fromDate, LocalDate toDate,
                                  LocalDate recordedAtDate);

    /**
     * Finds a discount based on the product, store, start date, and package details.
     *
     * @param product         The product.
     * @param store           The store.
     * @param fromDate        The start date of the discount period.
     * @param packageQuantity The package quantity.
     * @param packageUnit     The package unit.
     * @return An {@link Optional} containing the found discount, or empty if not found.
     */
    Optional<Discount> findDiscountByNaturalKey(Product product, Store store, LocalDate fromDate, BigDecimal packageQuantity, UnitOfMeasure packageUnit);


    /**
     * Finds a page of discounts that are currently active for a given date.
     *
     * @param date     The date to check for active discounts.
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} containing active {@link Discount} objects.
     */
    Page<Discount> findActiveDiscountsByDate(LocalDate date, Pageable pageable);

    /**
     * Finds all discounts for a specific product that are active on a given date.
     *
     * @param productId The ID of the product.
     * @param date      The date to check for active discounts.
     * @return A list of active {@link Discount} objects for the specified product.
     */
    List<Discount> findActiveDiscountsByProductAndDate(Long productId, LocalDate date);

    /**
     * Finds discounts that were recorded (added to the system) after a specific date.
     * Results are paginated.
     *
     * @param sinceDate The start date (exclusive or inclusive depending on requirement) for the recorded date.
     * @param pageable  Pagination and sorting information.
     * @return A {@link Page} of {@link Discount} objects recorded since the given date.
     */
    Page<Discount> findDiscountsRecordedAfter(LocalDate sinceDate, Pageable pageable);

    /**
     * Finds a page of the highest percentage discounts active on a given date.
     * Results are ordered by percentage descending.
     *
     * @param date      The date to check for active discounts.
     * @param pageable  Pagination information.
     * @return A {@link Page} of active discounts, ordered by percentage descending.
     */
    Page<Discount> findTopActiveDiscounts(LocalDate date, Pageable pageable);


    /**
     * Finds the best currently active discounts across all stores, ordered by discount percentage descending.
     * "Best" is defined by the highest discount percentage.
     *
     * @param referenceDate The date for which to check "current" active discounts (e.g., LocalDate.now()).
     * @param pageable      Pagination and sorting information. Note that primary sort will be by percentage.
     * @return A {@link Page} of {@link DiscountedProductDTO}s representing the best deals.
     */
    Page<DiscountedProductDTO> findBestActiveDiscounts(LocalDate referenceDate, Pageable pageable);

    Discount save(Discount discount);
}
