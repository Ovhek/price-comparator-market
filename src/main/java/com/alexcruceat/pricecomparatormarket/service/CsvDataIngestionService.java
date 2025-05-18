package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.exception.CsvProcessingException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Service interface for ingesting data from CSV files.
 * Defines methods for processing product price CSVs and discount CSVs.
 */
public interface CsvDataIngestionService {

    /**
     * Processes a single product price CSV file.
     * Reads product and price information, then persists it to the database.
     * This involves creating or updating {@link com.alexcruceat.pricecomparatormarket.model.Product},
     * {@link com.alexcruceat.pricecomparatormarket.model.Store}, {@link com.alexcruceat.pricecomparatormarket.model.Category},
     * {@link com.alexcruceat.pricecomparatormarket.model.Brand}, and {@link com.alexcruceat.pricecomparatormarket.model.PriceEntry} entities.
     *
     * @param csvFilePath The path to the product price CSV file.
     * @param storeName   The name of the store derived from the filename.
     * @param entryDate   The date derived from the filename, for which the prices are valid.
     * @throws IOException if an I/O error occurs reading the file.
     * @throws CsvProcessingException if an error occurs during CSV parsing or data processing.
     */
    void ingestProductPriceCsv(Path csvFilePath, String storeName, LocalDate entryDate) throws IOException;

    /**
     * Processes a single discount CSV file.
     * Reads discount information and persists it to the database.
     * This involves creating or updating {@link com.alexcruceat.pricecomparatormarket.model.Discount} entities,
     * linking them to existing products and stores.
     *
     * @param csvFilePath    The path to the discount CSV file.
     * @param storeName      The name of the store derived from the filename.
     * @param recordedAtDate The date derived from the filename, indicating when the discount info was recorded.
     * @throws IOException if an I/O error occurs reading the file.
     * @throws CsvProcessingException if an error occurs during CSV parsing or data processing.
     */
    void ingestDiscountCsv(Path csvFilePath, String storeName, LocalDate recordedAtDate) throws IOException;

    /**
     * Scans the configured input directory for all processable CSV files (both product prices and discounts)
     * and ingests them.
     * This method orchestrates the dynamic discovery and processing of files.
     */
    void ingestAllPendingCsvFiles();
}
