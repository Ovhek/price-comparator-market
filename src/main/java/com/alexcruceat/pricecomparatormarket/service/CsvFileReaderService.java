package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import de.siegmar.fastcsv.reader.CsvParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service responsible for reading and parsing CSV files into raw data transfer objects.
 * This service focuses solely on the file reading and initial parsing stages,
 * abstracting the complexities of the CSV format and library specifics.
 */
public interface CsvFileReaderService {

    /**
     * Reads a product price CSV file and converts its rows into a list of {@link ProductPriceCsvRow} objects.
     * Skips the header row and handles parsing errors for individual rows by logging a warning and continuing.
     *
     * @param filePath Path to the product price CSV file.
     * @return A list of {@link ProductPriceCsvRow} representing the valid data rows in the CSV.
     *         The list may be empty if the file is empty or all rows are malformed.
     * @throws IOException if an I/O error occurs while opening or reading the file.
     * @throws CsvParseException if a FastCSV specific structural parsing error occurs (e.g., unclosed quotes affecting the whole file).
     * @throws com.alexcruceat.pricecomparatormarket.exception.CsvProcessingException if a fatal error occurs during processing, such as an invalid header that prevents further processing.
     */
    List<ProductPriceCsvRow> readProductPriceCsv(Path filePath) throws IOException, CsvParseException;

    /**
     * Reads a discount CSV file and converts its rows into a list of {@link DiscountCsvRow} objects.
     * Skips the header row and handles parsing errors for individual rows by logging a warning and continuing.
     *
     * @param filePath Path to the discount CSV file.
     * @return A list of {@link DiscountCsvRow} representing the valid data rows in the CSV.
     *         The list may be empty if the file is empty or all rows are malformed.
     * @throws IOException if an I/O error occurs while opening or reading the file.
     * @throws CsvParseException if a FastCSV specific structural parsing error occurs.
     * @throws com.alexcruceat.pricecomparatormarket.exception.CsvProcessingException if a fatal error occurs during processing, such as an invalid header.
     */
    List<DiscountCsvRow> readDiscountCsv(Path filePath) throws IOException, CsvParseException;
}
