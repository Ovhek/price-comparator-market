package com.alexcruceat.pricecomparatormarket.service;
import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link PriceEntry} entities.
 */
public interface PriceEntryService {

    /**
     * Saves or updates a price entry for a given product, store, and date.
     * If an entry exists for the specified combination, it's updated. Otherwise, a new one is created.
     *
     * @param product         The product for the price entry.
     * @param store           The store for the price entry.
     * @param entryDate       The date of the price entry.
     * @param storeProductId  The store's specific ID for the product.
     * @param price           The price.
     * @param currency        The currency.
     * @param packageQuantity The package quantity.
     * @param unit            The unit of measure for the package.
     * @return The saved or updated {@link PriceEntry}.
     */
    PriceEntry saveOrUpdatePriceEntry(Product product, Store store, LocalDate entryDate,
                                      String storeProductId, BigDecimal price, String currency,
                                      BigDecimal packageQuantity, UnitOfMeasure unit);

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

    PriceEntry save(PriceEntry newPriceEntry);

    /**
     * Finds all price entries for a specific product within a store and given date range,
     * ordered by entry date ascending.
     *
     * @param productId The ID of the product.
     * @param storeId The ID of the Store.
     * @param startDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return A list of {@link PriceEntry} objects for the product within the date range.
     */
    List<PriceEntry> getPriceEntriesForProductAndStore(Long productId, Long storeId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds all price entries for a specific product within a given date range,
     * ordered by entry date ascending.
     *
     * @param productId The ID of the product.
     * @param startDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return A list of {@link PriceEntry} objects for the product within the date range.
     */
    List<PriceEntry> getPriceEntriesForProduct(Long productId, LocalDate startDate, LocalDate endDate);

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
    List<PriceEntry> findLatestPriceEntriesPerStoreForProduct(Long productId, LocalDate referenceDate);

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
     * Retrieves the most recent price entry for a given product and store that matches the
     * specified package quantity and unit, where the entry date is less than or equal to
     * the given reference date.
     *
     * @param product         the product for which the price is being queried (must not be null)
     * @param store           the store for which the price is being queried (must not be null)
     * @param packageQuantity the quantity of the product's package (must not be null)
     * @param packageUnit     the unit of measure for the package (must not be null)
     * @param referenceDate   the latest acceptable entry date
     * @return an {@code Optional<PriceEntry>} containing the most recent applicable price entry, if available
     */
    Optional<PriceEntry> findFirstByProductAndStoreAndPackageQuantityAndPackageUnitAndEntryDateLessThanEqualOrderByEntryDateDesc(@NotNull(message = "Product for discount cannot be null.") Product product, @NotNull(message = "Store for discount cannot be null.") Store store, @NotNull(message = "Discount package quantity cannot be null.") BigDecimal packageQuantity, @NotNull(message = "Discount package unit cannot be null.") UnitOfMeasure packageUnit, LocalDate referenceDate);

}
