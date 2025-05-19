package com.alexcruceat.pricecomparatormarket.util;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing information from CSV filenames.
 * Expected filename formats:
 * - Product Prices: storename_yyyy-MM-dd.csv (e.g., lidl_2025-05-08.csv)
 * - Discounts: storename_discount_yyyy-MM-dd.csv (e.g., lidl_discount_2025-05-08.csv)
 */
@Slf4j
public class CsvFilenameParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    private static final Pattern PRODUCT_PRICE_CSV_PATTERN = Pattern.compile("^([a-zA-Z0-9]+)_(\\d{4}-\\d{2}-\\d{2})\\.csv$");
    private static final Pattern DISCOUNT_CSV_PATTERN = Pattern.compile("^([a-zA-Z0-9]+)_discounts_(\\d{4}-\\d{2}-\\d{2})\\.csv$");

    /**
     * Represents the type of CSV file.
     */
    public enum CsvFileType {
        PRODUCT_PRICE,
        DISCOUNT,
        UNKNOWN
    }

    /**
     * Holds the parsed information from a CSV filename.
     */
    @Value
    public static class ParsedCsvInfo {
        CsvFileType fileType;
        String storeName;
        LocalDate date;
    }

    /**
     * Parses the given filename to extract store name, date, and file type.
     *
     * @param filename The name of the CSV file (e.g., "lidl_2025-05-08.csv" or "profi_discount_2025-05-01.csv").
     * @return {@link ParsedCsvInfo} containing the extracted data, or info with {@link CsvFileType#UNKNOWN} if parsing fails.
     */
    public static ParsedCsvInfo parseFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            log.warn("Attempted to parse a null or empty filename.");
            return new ParsedCsvInfo(CsvFileType.UNKNOWN, null, null);
        }

        Matcher productMatcher = PRODUCT_PRICE_CSV_PATTERN.matcher(filename);
        if (productMatcher.matches()) {
            String storeName = productMatcher.group(1);
            String dateStr = productMatcher.group(2);
            try {
                LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                log.debug("Parsed product price CSV: '{}', store: '{}', date: '{}'", filename, storeName, date);
                return new ParsedCsvInfo(CsvFileType.PRODUCT_PRICE, storeName, date);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date from product CSV filename '{}'. Date string: '{}'. Error: {}", filename, dateStr, e.getMessage());
                return new ParsedCsvInfo(CsvFileType.UNKNOWN, storeName, null);
            }
        }

        Matcher discountMatcher = DISCOUNT_CSV_PATTERN.matcher(filename);
        if (discountMatcher.matches()) {
            String storeName = discountMatcher.group(1);
            String dateStr = discountMatcher.group(2);
            try {
                LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                log.debug("Parsed discount CSV: '{}', store: '{}', date: '{}'", filename, storeName, date);
                return new ParsedCsvInfo(CsvFileType.DISCOUNT, storeName, date);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date from discount CSV filename '{}'. Date string: '{}'. Error: {}", filename, dateStr, e.getMessage());
                return new ParsedCsvInfo(CsvFileType.UNKNOWN, storeName, null);
            }
        }

        log.warn("Filename '{}' did not match any known CSV pattern.", filename);
        return new ParsedCsvInfo(CsvFileType.UNKNOWN, null, null);
    }

    // Private constructor to prevent instantiation of utility class
    private CsvFilenameParser() {}
}
