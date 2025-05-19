package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;


import java.time.LocalDate;

/**
 * Service responsible for processing individual product price data rows
 * and persisting them as {@link Product}
 * and {@link PriceEntry} entities.
 */
public interface ProductDataHandlerService {

    /**
     * Processes a single row of product price data from a CSV.
     * This involves:
     * 1. Finding or creating the {@link Brand}.
     * 2. Finding or creating the {@link Category}.
     * 3. Finding or creating the {@link Product}.
     * 4. Finding or creating/updating the {@link PriceEntry}.
     * <p>
     * This method is expected to be called within an existing transaction, typically
     * managed by the calling service (e.g., CsvDataIngestionService).
     *
     * @param priceData The raw product price data parsed from a CSV row.
     * @param store     The {@link Store} entity to which this price data belongs.
     * @param entryDate The date for which this price entry is valid.
     */
    void processAndSaveProductPrice(ProductPriceCsvRow priceData, Store store, LocalDate entryDate);
}
