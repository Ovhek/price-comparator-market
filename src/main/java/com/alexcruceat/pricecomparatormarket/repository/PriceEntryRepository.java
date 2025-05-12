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
     * Finds all price entries for a specific product within a given date range,
     * ordered by entry date ascending.
     *
     * @param productId The ID of the product.
     * @param startDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return A list of {@link PriceEntry} objects for the product within the date range.
     */
    @Query("SELECT pe FROM PriceEntry pe WHERE pe.product.id = :productId AND pe.entryDate BETWEEN :startDate AND :endDate ORDER BY pe.entryDate ASC")
    List<PriceEntry> findProductPriceHistoryInRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
