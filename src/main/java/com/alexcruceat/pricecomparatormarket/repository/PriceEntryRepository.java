package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PriceEntry} entities.
 */
@Repository
public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {

    /**
     * Finds a specific price entry based on the product, store, and date.
     *
     * @param product The product.
     * @param store   The store.
     * @param date    The entry date.
     * @return An {@link Optional} containing the found price entry, or empty if not found.
     */
    Optional<PriceEntry> findByProductAndStoreAndEntryDate(Product product, Store store, LocalDate date);

    /**
     * Finds all price entries for a specific product, ordered by entry date descending.
     * Useful for retrieving price history.
     *
     * @param product The product whose price history is requested.
     * @return A list of {@link PriceEntry} objects ordered by date descending.
     */
    List<PriceEntry> findByProductOrderByEntryDateDesc(Product product);

    /**
     * Finds all price entries for a specific product and store within a given date range,
     * ordered by entry date ascending.
     *
     * @param productId The ID of the product.
     * @param storeId   The ID of the store.
     * @param startDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return A list of {@link PriceEntry} objects.
     */
    List<PriceEntry> findByProductIdAndStoreIdAndEntryDateBetweenOrderByEntryDateAsc(
            Long productId,
            Long storeId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Finds all price entries for a specific product within a given date range,
     * ordered by entry date ascending.
     *
     * @param productId The ID of the product.
     * @param startDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return A list of {@link PriceEntry} objects.
     */
    List<PriceEntry> findByProductIdAndEntryDateBetweenOrderByEntryDateAsc(
            Long productId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Retrieves a list of {@link PriceEntry} records for a specific product category
     * within a given date range and optionally filtered by store.
     *
     * @param categoryId the ID of the product category to filter by (required)
     * @param storeId the ID of the store to filter by (optional; can be {@code null})
     * @param startDate the start date of the period to retrieve entries for (inclusive)
     * @param endDate the end date of the period to retrieve entries for (inclusive)
     * @return a list of {@link PriceEntry} objects matching the criteria,
     *         ordered by entry date in ascending order
     */
    @Query("SELECT pe FROM PriceEntry pe JOIN pe.product p WHERE p.category.id = :categoryId " +
            "AND (:storeId IS NULL OR pe.store.id = :storeId) " +
            "AND pe.entryDate BETWEEN :startDate AND :endDate " +
            "ORDER BY pe.entryDate ASC")
    List<PriceEntry> findPriceEntriesForCategoryTrend(
            @Param("categoryId") Long categoryId,
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Retrieves a list of {@link PriceEntry} records for a specific brand
     * within a given date range and optionally filtered by store.
     *
     * @param brandId the ID of the brand to filter by (required)
     * @param storeId the ID of the store to filter by (optional; can be {@code null})
     * @param startDate the start date of the period to retrieve entries for (inclusive)
     * @param endDate the end date of the period to retrieve entries for (inclusive)
     * @return a list of {@link PriceEntry} objects matching the criteria,
     *         ordered by entry date in ascending order
     */
    @Query("SELECT pe FROM PriceEntry pe JOIN pe.product p WHERE p.brand.id = :brandId " +
            "AND (:storeId IS NULL OR pe.store.id = :storeId) " +
            "AND pe.entryDate BETWEEN :startDate AND :endDate " +
            "ORDER BY pe.entryDate ASC")
    List<PriceEntry> findPriceEntriesForBrandTrend(
            @Param("brandId") Long brandId,
            @Param("storeId") Long storeId, // Can be null
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    /**
     * Retrieves a list of {@link PriceEntry} records for a specific product over a given date range,
     * optionally filtered by store.
     *
     * <p>If {@code storeId} is {@code null}, the store filter is ignored and entries from all stores are included.</p>
     *
     * @param productId the ID of the product to fetch price history for (required)
     * @param storeId the ID of the store to filter by (nullable)
     * @param startDate the start date of the date range (inclusive)
     * @param endDate the end date of the date range (inclusive)
     * @return a chronologically ordered list of price entries matching the criteria
     */
    @Query("SELECT pe FROM PriceEntry pe WHERE pe.product.id = :productId " +
            "AND (:storeId IS NULL OR pe.store.id = :storeId) " +
            "AND pe.entryDate BETWEEN :startDate AND :endDate " +
            "ORDER BY pe.entryDate ASC")
    List<PriceEntry> findIndividualProductHistory(
            @Param("productId") Long productId,
            @Param("storeId") Long storeId, // Can be null
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
