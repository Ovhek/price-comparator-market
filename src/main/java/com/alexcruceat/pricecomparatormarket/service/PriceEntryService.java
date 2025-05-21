package com.alexcruceat.pricecomparatormarket.service;
import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;

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



}
