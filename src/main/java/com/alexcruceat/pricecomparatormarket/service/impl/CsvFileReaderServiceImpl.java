package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.exception.CsvProcessingException;
import com.alexcruceat.pricecomparatormarket.service.CsvFileReaderService;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.reader.CsvParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link CsvFileReaderService}
 * This class handles the low-level details of reading CSV files and transforming
 * rows into specific DTOs ({@link ProductPriceCsvRow}, {@link DiscountCsvRow}).
 * It includes basic validation for row structure and data types.
 */
@Service
@Slf4j
public class CsvFileReaderServiceImpl implements CsvFileReaderService {

    private static final int PRODUCT_PRICE_EXPECTED_FIELD_COUNT = 8;
    private static final int DISCOUNT_EXPECTED_FIELD_COUNT = 9;
    private static final List<String> EXPECTED_PRODUCT_PRICE_HEADER = Arrays.asList(
            "product_id", "product_name", "product_category", "brand",
            "package_quantity", "package_unit", "price", "currency"
    );
    private static final List<String> EXPECTED_DISCOUNT_HEADER = Arrays.asList(
            "product_id", "product_name", "brand", "package_quantity", "package_unit",
            "product_category", "from_date", "to_date", "percentage_of_discount"
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductPriceCsvRow> readProductPriceCsv(Path filePath) throws IOException, CsvParseException {
        log.debug("Reading product price CSV: {}", filePath);
        List<ProductPriceCsvRow> rows = new ArrayList<>();
        try (CsvReader<CsvRecord> reader = CsvReader.builder()
                .fieldSeparator(';')
                .quoteCharacter('"')
                .skipEmptyLines(true)
                .ofCsvRecord(filePath, StandardCharsets.UTF_8)) {

            boolean isHeader = true;
            for (CsvRecord record : reader) {
                long currentLine = record.getStartingLineNumber();
                if (isHeader) {
                    validateHeader(record, EXPECTED_PRODUCT_PRICE_HEADER, filePath.toString(), currentLine);
                    isHeader = false;
                    continue;
                }
                try {
                    rows.add(parseProductPriceRow(record, filePath.toString()));
                } catch (CsvProcessingException e) {
                    log.warn("Skipping problematic row at line {} in product price CSV {}: {}", currentLine, filePath, e.getMessage());
                }
            }
        }
        log.info("Read {} product price data rows from {}", rows.size(), filePath);
        return rows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DiscountCsvRow> readDiscountCsv(Path filePath) throws IOException, CsvParseException {
        log.debug("Reading discount CSV: {}", filePath);
        List<DiscountCsvRow> rows = new ArrayList<>();
        try (CsvReader<CsvRecord> reader = CsvReader.builder()
                .fieldSeparator(';')
                .quoteCharacter('"')
                .skipEmptyLines(true)
                .ofCsvRecord(filePath, StandardCharsets.UTF_8)) {

            boolean isHeader = true;
            for (CsvRecord record : reader) {
                long currentLine = record.getStartingLineNumber();
                if (isHeader) {
                    validateHeader(record, EXPECTED_DISCOUNT_HEADER, filePath.toString(), currentLine);
                    isHeader = false;
                    continue;
                }
                try {
                    rows.add(parseDiscountRow(record, filePath.toString()));
                } catch (CsvProcessingException e) {
                    log.warn("Skipping problematic row at line {} in discount CSV {}: {}", currentLine, filePath, e.getMessage());
                }
            }
        }
        log.info("Read {} discount data rows from {}", rows.size(), filePath);
        return rows;
    }

    /**
     * Parses a single {@link CsvRecord} from a product price CSV into a {@link ProductPriceCsvRow}.
     *
     * @param record The CsvRecord to parse.
     * @param filePath The path of the CSV file being processed (for logging/error context).
     * @return The parsed {@link ProductPriceCsvRow}.
     * @throws CsvProcessingException if the row has an incorrect number of fields or if data conversion fails.
     */
    private ProductPriceCsvRow parseProductPriceRow(CsvRecord record, String filePath) {
        long currentLine = record.getStartingLineNumber();
        if (record.getFieldCount() != PRODUCT_PRICE_EXPECTED_FIELD_COUNT) {
            throw new CsvProcessingException(String.format("Line %d in %s: Expected %d fields, but found %d. Row: %s",
                    currentLine, filePath, PRODUCT_PRICE_EXPECTED_FIELD_COUNT, record.getFieldCount(), record.getFields()));
        }
        try {
            return ProductPriceCsvRow.builder()
                    .productId(record.getField(0))
                    .productName(record.getField(1))
                    .productCategory(record.getField(2))
                    .brand(record.getField(3))
                    .packageQuantity(parseBigDecimal(record.getField(4), currentLine, filePath, "package_quantity"))
                    .packageUnit(record.getField(5))
                    .price(parseBigDecimal(record.getField(6), currentLine, filePath, "price"))
                    .currency(record.getField(7))
                    .build();
        } catch (Exception e) {
            throw new CsvProcessingException(String.format("Line %d in %s: Error parsing product price row. Row: %s",
                    currentLine, filePath, record.getFields()), e);
        }
    }

    /**
     * Parses a single {@link CsvRecord} from a discount CSV into a {@link DiscountCsvRow}.
     *
     * @param record The CsvRecord to parse.
     * @param filePath The path of the CSV file being processed (for logging/error context).
     * @return The parsed {@link DiscountCsvRow}.
     * @throws CsvProcessingException if the row has an incorrect number of fields or if data conversion fails.
     */
    private DiscountCsvRow parseDiscountRow(CsvRecord record, String filePath) {
        long currentLine = record.getStartingLineNumber();
        if (record.getFieldCount() != DISCOUNT_EXPECTED_FIELD_COUNT) {
            throw new CsvProcessingException(String.format("Line %d in %s: Expected %d fields, but found %d. Row: %s",
                    currentLine, filePath, DISCOUNT_EXPECTED_FIELD_COUNT, record.getFieldCount(), record.getFields()));
        }
        try {
            return DiscountCsvRow.builder()
                    .productId(record.getField(0))
                    .productName(record.getField(1))
                    .brand(record.getField(2))
                    .packageQuantity(parseBigDecimal(record.getField(3), currentLine, filePath, "package_quantity"))
                    .packageUnit(record.getField(4))
                    .productCategory(record.getField(5))
                    .fromDate(parseLocalDate(record.getField(6), currentLine, filePath, "from_date"))
                    .toDate(parseLocalDate(record.getField(7), currentLine, filePath, "to_date"))
                    .percentageOfDiscount(parseInteger(record.getField(8), currentLine, filePath))
                    .build();
        } catch (Exception e) {
            throw new CsvProcessingException(String.format("Line %d in %s: Error parsing discount row. Row: %s",
                    currentLine, filePath, record.getFields()), e);
        }
    }

    /**
     * Parses a string value into a {@link BigDecimal}.
     * Handles comma as a decimal separator by replacing it with a dot.
     *
     * @param value      The string value to parse.
     * @param lineNumber The line number in the CSV file (for error reporting).
     * @param filePath   The path of the CSV file (for error reporting).
     * @param fieldName  The name of the field being parsed (for error reporting).
     * @return The parsed {@link BigDecimal}.
     * @throws CsvProcessingException if the value cannot be parsed into a BigDecimal.
     */
    private BigDecimal parseBigDecimal(String value, long lineNumber, String filePath, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CsvProcessingException(String.format("Line %d in %s: Missing BigDecimal value for field '%s'.",
                    lineNumber, filePath, fieldName));
        }
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new CsvProcessingException(String.format("Line %d in %s: Invalid BigDecimal value '%s' for field '%s'.",
                    lineNumber, filePath, value, fieldName), e);
        }
    }

    /**
     * Parses a string value into a {@link LocalDate} using ISO_LOCAL_DATE format (yyyy-MM-dd).
     *
     * @param value      The string value to parse.
     * @param lineNumber The line number in the CSV file (for error reporting).
     * @param filePath   The path of the CSV file (for error reporting).
     * @param fieldName  The name of the field being parsed (for error reporting).
     * @return The parsed {@link LocalDate}.
     * @throws CsvProcessingException if the value cannot be parsed into a LocalDate.
     */
    private LocalDate parseLocalDate(String value, long lineNumber, String filePath, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CsvProcessingException(String.format("Line %d in %s: Missing LocalDate value for field '%s'.",
                    lineNumber, filePath, fieldName));
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new CsvProcessingException(String.format("Line %d in %s: Invalid LocalDate value '%s' for field '%s'. Expected yyyy-MM-dd.",
                    lineNumber, filePath, value, fieldName), e);
        }
    }

    /**
     * Parses a string value into an {@code int}.
     *
     * @param value      The string value to parse.
     * @param lineNumber The line number in the CSV file (for error reporting).
     * @param filePath   The path of the CSV file (for error reporting).
     * @return The parsed {@code int}.
     * @throws CsvProcessingException if the value cannot be parsed into an int.
     */
    private int parseInteger(String value, long lineNumber, String filePath) {
        if (value == null || value.trim().isEmpty()) {
            throw new CsvProcessingException(String.format("Line %d in %s: Missing Integer value for field '%s'.",
                    lineNumber, filePath, "percentage_of_discount"));
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new CsvProcessingException(String.format("Line %d in %s: Invalid Integer value '%s' for field '%s'.",
                    lineNumber, filePath, value, "percentage_of_discount"), e);
        }
    }

    /**
     * Validates the header row of a CSV file against an expected list of header names.
     * Logs a warning if a mismatch is found.
     *
     * @param headerRecord   The {@link CsvRecord} representing the header row.
     * @param expectedHeader A list of expected header strings.
     * @param filePath       The path of the CSV file (for logging).
     * @param lineNumber     The line number of the header (for logging).
     */
    private void validateHeader(CsvRecord headerRecord, List<String> expectedHeader, String filePath, long lineNumber) {
        List<String> actualHeader = headerRecord.getFields();
        if (!actualHeader.equals(expectedHeader)) {
            String errorMsg = String.format("File %s: Invalid header at line %d. Expected: %s, Actual: %s",
                    filePath, lineNumber, expectedHeader, actualHeader);
            log.error(errorMsg);
            throw new CsvProcessingException(errorMsg);
        }
    }
}
