package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Discount;

import java.time.LocalDate;

/**
 * Service responsible for processing individual discount data rows
 * and persisting them as {@link Discount} entities.
 */
public interface DiscountDataHandlerService {

    /**
     * Processes a single row of discount data from a CSV.
     * This involves:
     * 1. Finding the associated {@link Product}
     * 2. Finding or creating/updating the {@link Discount}.
     * <p>
     * This method is expected to be called within an existing transaction.
     *
     * @param discountData   The raw discount data parsed from a CSV row.
     * @param store          The {@link Store} entity offering this discount.
     * @param recordedAtDate The date when this discount information was recorded (from CSV filename).
     */
    void processAndSaveDiscount(DiscountCsvRow discountData, Store store, LocalDate recordedAtDate);
}
