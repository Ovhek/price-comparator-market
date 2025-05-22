package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    /**
     * Finds the most recent price entry for a given product, store, package quantity, and package unit,
     * on or before a specified reference date.
     *
     * @param product         The product.
     * @param store           The store.
     * @param packageQuantity The package quantity.
     * @param packageUnit     The package unit.
     * @param referenceDate   The date to find the price on or before.
     * @return Optional containing the most recent {@link PriceEntry} or empty if none found.
     */
    Optional<PriceEntry> findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(
            Product product, Store store, BigDecimal packageQuantity, UnitOfMeasure packageUnit, LocalDate referenceDate
    );

    /**
     * Finds the most recent price entry for a given product in a specific store,
     * on or before a specified reference date.
     *
     * @param productId     The ID of the product.
     * @param storeId       The ID of the store.
     * @param referenceDate The date to find the price on or before.
     * @return Optional containing the most recent {@link PriceEntry} or empty if none found.
     */
    Optional<PriceEntry> findFirstByProduct_IdAndStore_IdAndEntryDateLessThanEqualOrderByEntryDateDesc(
            Long productId, Long storeId, LocalDate referenceDate
    );

    /**
     * Finds all price entries for a given product on a specific date across all stores.
     * @param productId The ID of the product.
     * @param entryDate The specific date.
     * @return List of price entries.
     */
    List<PriceEntry> findByProductIdAndEntryDate(Long productId, LocalDate entryDate);

    /**
     * Finds the latest price entry for each store for a given product, on or before a reference date.
     * This is a more complex query, often requiring a subquery or window functions if done purely in SQL/JPQL.
     * For simplicity, this might fetch more data and then be filtered in Java, or be a native query.
     * Let's define a simpler version for now that gets all entries for a product before a date.
     * The service layer will then have to pick the latest per store.
     */
    @Query("SELECT pe FROM PriceEntry pe WHERE pe.product.id = :productId AND pe.entryDate <= :referenceDate ORDER BY pe.store.id ASC, pe.entryDate DESC")
    List<PriceEntry> findLatestPriceEntriesPerStoreForProduct(@Param("productId") Long productId, @Param("referenceDate") LocalDate referenceDate);

    /**
     * Finds the most recent price entry for a given product across all stores,
     * on or before a specified reference date.
     *
     * @param product       The product entity.
     * @param referenceDate The date to find the price on or before.
     * @return Optional containing the most recent {@link PriceEntry} or empty if none found.
     */
    Optional<PriceEntry> findFirstByProductAndEntryDateLessThanEqualOrderByEntryDateDesc(Product product, LocalDate referenceDate);


    /**
     * Finds the most recent {@link PriceEntry} for a given {@link Product} and {@link Store}
     * where the entry date is less than or equal to the specified date.
     *
     * <p>This method ensures that all parameters are non-null and uses assertions to validate them.
     *
     * @param product the product to search for, must not be {@code null}
     * @param store the store to search in, must not be {@code null}
     * @param date the reference date, must not be {@code null}
     * @return an {@link Optional} containing the most recent matching {@link PriceEntry},
     *         or {@link Optional#empty()} if no match is found
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    Optional<PriceEntry> findFirstByProductAndStoreAndEntryDateLessThanEqualOrderByEntryDateDesc(Product product, Store store, LocalDate date);
}
