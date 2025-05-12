package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Discount;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Discount} entities.
 */
@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    /**
     * Finds a discount based on the product, store, and start date.
     *
     * @param product  The product.
     * @param store    The store.
     * @param fromDate The start date of the discount period.
     * @return An {@link Optional} containing the found discount, or empty if not found.
     */
    Optional<Discount> findByProductAndStoreAndFromDate(Product product, Store store, LocalDate fromDate);

    /**
     * Finds a page of discounts that are currently active for a given date.
     * A discount is active if the given date falls between its fromDate and toDate (inclusive).
     * Results can be paginated and sorted using the Pageable parameter.
     *
     * @param date The date to check for active discounts.
     * @param pageable Pagination and sorting information.
     * @return A list of {@link Discount} objects active on the specified date.
     */
    @Query("SELECT d FROM Discount d WHERE :date BETWEEN d.fromDate AND d.toDate")
    Page<Discount> findActiveDiscountsByDate(@Param("date") LocalDate date, Pageable pageable);


    /**
     * Finds all discounts for a specific product that are active on a given date.
     *
     * @param productId The ID of the product.
     * @param date      The date to check for active discounts.
     * @return A list of active {@link Discount} objects for the specified product.
     */
    @Query("SELECT d FROM Discount d WHERE d.product.id = :productId AND :date BETWEEN d.fromDate AND d.toDate")
    List<Discount> findActiveDiscountsByProductAndDate(@Param("productId") Long productId, @Param("date") LocalDate date);


    /**
     * Finds discounts that were recorded (added to the system) within a specific timeframe,
     * ordered by recorded date descending by default via Pageable sort or method name convention.
     * Useful for the "New Discounts" feature. Results are paginated.
     *
     * @param sinceDate The start date (exclusive or inclusive depending on requirement) for the recorded date.
     * @param pageable  Pagination and sorting information (e.g., sort by recordedAtDate descending).
     * @return A {@link Page} of {@link Discount} objects recorded since the given date.
     */
    Page<Discount> findByRecordedAtDateAfter(LocalDate sinceDate, Pageable pageable);

    /**
     * Finds a page of the highest percentage discounts active on a given date.
     * Results are ordered by percentage descending.
     *
     * @param date      The date to check for active discounts.
     * @param pageable  Pagination information (size, page number). Sorting is predefined by percentage.
     * @return A {@link Page} of active discounts, ordered by percentage descending.
     */
    @Query("SELECT d FROM Discount d WHERE :date BETWEEN d.fromDate AND d.toDate ORDER BY d.percentage DESC, d.product.name ASC")
    Page<Discount> findTopActiveDiscounts(@Param("date") LocalDate date, Pageable pageable);
}
