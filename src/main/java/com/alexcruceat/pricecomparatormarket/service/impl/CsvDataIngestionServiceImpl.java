package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.config.AppProperties;
import com.alexcruceat.pricecomparatormarket.exception.CsvProcessingException;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.repository.*;
import com.alexcruceat.pricecomparatormarket.service.*;
import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import com.alexcruceat.pricecomparatormarket.util.CsvFilenameParser;
import de.siegmar.fastcsv.reader.CsvParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Orchestrates the ingestion of CSV data.
 * Manages transactions at the file level.
 */
@Service
@Slf4j
public class CsvDataIngestionServiceImpl implements CsvDataIngestionService, ApplicationRunner {

    private final AppProperties appProperties;
    private final CsvFileReaderService csvFileReaderService;
    private final ProductDataHandlerService productDataHandlerService;
    private final DiscountDataHandlerService discountDataHandlerService;
    private final StoreService storeService;
    private CsvDataIngestionService self;

    public CsvDataIngestionServiceImpl(AppProperties appProperties,
                                       CsvFileReaderService csvFileReaderService,
                                       StoreService storeService,
                                       ProductDataHandlerService productDataHandlerService,
                                       DiscountDataHandlerService discountDataHandlerService) {
        this.appProperties = appProperties;
        this.csvFileReaderService = csvFileReaderService;
        this.storeService = storeService;
        this.productDataHandlerService = productDataHandlerService;
        this.discountDataHandlerService = discountDataHandlerService;
    }

    @Autowired
    public void setSelf(@Lazy CsvDataIngestionService self) {
        this.self = self;
    }

    /**
     * {@inheritDoc}
     *    * This method is called during application startup via the {@link ApplicationRunner} interface
     *      * AND is scheduled to run periodically to pick up new files.
     *      * <p>
     *      * The scheduling interval can be configured in application properties.
     *      * Example cron: "0 0 * * * ?" (every hour at the start of the hour)
     */
    @Override
    @Scheduled(cron = "${app.csv.ingestion.cron:0 0 */1 * * ?}")
    public void ingestAllPendingCsvFiles() {
        log.info("Scheduled CSV ingestion task started.");

        Path inputDir = Paths.get(appProperties.getCsv().getInputPath());
        if (!Files.isDirectory(inputDir)) {
            log.error("CSV input path '{}' is not a directory or does not exist. Scheduled ingestion skipped.", inputDir);
            return;
        }

        log.info("Scheduled scan of CSV files in directory: {}", inputDir);
        try (Stream<Path> paths = Files.list(inputDir)) {
            paths.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .forEach(this::processSingleCsvFile); // Calls internal method to process each file
        } catch (IOException e) {
            log.error("Error reading CSV input directory during scheduled task: {}. Ingestion may be incomplete.", inputDir, e);
        }
        log.info("Scheduled CSV ingestion task finished.");
    }

    /**
     * Orchestrates the processing of a single CSV file.
     * This method is not transactional itself but calls public transactional methods via self-injection.
     * @param csvFilePath Path to the CSV file.
     */
    private void processSingleCsvFile(Path csvFilePath) {
        log.info("Processing CSV file: {}", csvFilePath);
        CsvFilenameParser.ParsedCsvInfo parsedInfo = CsvFilenameParser.parseFilename(csvFilePath.getFileName().toString());

        try {
            switch (parsedInfo.getFileType()) {
                case PRODUCT_PRICE:
                    self.ingestProductPriceCsv(csvFilePath, parsedInfo.getStoreName(), parsedInfo.getDate());
                    log.info("Successfully processed product price CSV: {}", csvFilePath);
                    break;
                case DISCOUNT:
                    self.ingestDiscountCsv(csvFilePath, parsedInfo.getStoreName(), parsedInfo.getDate());
                    log.info("Successfully processed discount CSV: {}", csvFilePath);
                    break;
                case UNKNOWN:
                    log.warn("Skipping file with unknown CSV format: {}", csvFilePath);
                    break;
                default:
                    log.warn("Unhandled CSV file type for file: {}", csvFilePath);
                    break;
            }
            String processedPathConfig = appProperties.getCsv().getProcessedPath();
            if (processedPathConfig != null && !processedPathConfig.isBlank()) {
                moveFileToProcessed(csvFilePath);
            }
        } catch (IOException e) {
            log.error("I/O error during overall processing of file {}: {}", csvFilePath, e.getMessage(), e);
        } catch (CsvParseException e) {
            log.error("CSV parsing structure error for file {}: {}", csvFilePath, e.getMessage(), e);
        } catch (CsvProcessingException e) {
            log.error("CSV data processing error for file {}: {}", csvFilePath, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing file {}: {}", csvFilePath, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {IOException.class, CsvProcessingException.class, CsvParseException.class, RuntimeException.class})
    public void ingestProductPriceCsv(Path csvFilePath, String storeName, LocalDate entryDate) throws IOException, CsvParseException {
        log.info("Ingesting product prices from: {}, Store: {}, Date: {}", csvFilePath, storeName, entryDate);
        Store store = storeService.findOrCreateStore(storeName);

        List<ProductPriceCsvRow> priceDataRows = csvFileReaderService.readProductPriceCsv(csvFilePath);

        if (priceDataRows.isEmpty() && Files.size(csvFilePath) > 0) { // Check if file was not empty but yielded no rows
            log.warn("No processable product price rows found in non-empty file: {}. It might contain only a header or malformed rows.", csvFilePath);
        }


        for (ProductPriceCsvRow rowData : priceDataRows) {
            try {
                productDataHandlerService.processAndSaveProductPrice(rowData, store, entryDate);
            } catch (Exception e) {
                // Current CsvFileReaderService skips bad rows during reading and logs them.
                log.error("Critical error processing product price row for store '{}', date '{}', data '{}'. File: {}. Error: {}",
                        storeName, entryDate, rowData, csvFilePath, e.getMessage(), e);
                if (e instanceof CsvProcessingException) {
                    log.warn("Skipping product price row due to processing error: {}", e.getMessage());
                } else {
                    throw new CsvProcessingException("Critical error processing product price row: " + e.getMessage(), e);
                }
            }
        }
        log.info("Finished transactional ingestion of product prices from: {}", csvFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {IOException.class, CsvProcessingException.class, CsvParseException.class, RuntimeException.class})
    public void ingestDiscountCsv(Path csvFilePath, String storeName, LocalDate recordedAtDate) throws IOException, CsvParseException {
        log.info("Ingesting discounts from: {}, Store: {}, RecordedAtDate: {}", csvFilePath, storeName, recordedAtDate);
        Store store = storeService.findOrCreateStore(storeName);

        List<DiscountCsvRow> discountDataRows = csvFileReaderService.readDiscountCsv(csvFilePath);

        if (discountDataRows.isEmpty() && Files.size(csvFilePath) > 0) {
            log.warn("No processable discount rows found in non-empty file: {}. It might contain only a header or malformed rows.", csvFilePath);
        }

        for (DiscountCsvRow rowData : discountDataRows) {
            try {
                discountDataHandlerService.processAndSaveDiscount(rowData, store, recordedAtDate);
            } catch (Exception e) {
                log.error("Critical error processing discount row for store '{}', recordedAt '{}', data '{}'. File: {}. Error: {}",
                        storeName, recordedAtDate, rowData, csvFilePath, e.getMessage(), e);
                if (e instanceof CsvProcessingException) {
                    log.warn("Skipping discount row due to processing error: {}", e.getMessage());
                } else {
                    throw new CsvProcessingException("Critical error processing discount row: " + e.getMessage(), e);
                }
            }
        }
        log.info("Finished transactional ingestion of discounts from: {}", csvFilePath);
    }

    /**
     * Moves a processed file to the configured 'processed' directory.
     * Handles potential filename collisions by appending a timestamp.
     *
     * @param sourceFile The file to move.
     */
    private void moveFileToProcessed(Path sourceFile) {
        String processedPathConfig = appProperties.getCsv().getProcessedPath();
        if (processedPathConfig == null || processedPathConfig.isBlank()) {
            log.debug("Processed path not configured. File {} will not be moved.", sourceFile);
            return;
        }
        Path processedDirPath = Paths.get(processedPathConfig);
        try {
            if (!Files.exists(processedDirPath)) {
                Files.createDirectories(processedDirPath);
                log.info("Created processed directory: {}", processedDirPath);
            }

            String originalFilename = sourceFile.getFileName().toString();
            Path targetFile = processedDirPath.resolve(originalFilename);

            if (Files.exists(targetFile)) {
                String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
                String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                targetFile = processedDirPath.resolve(String.format("%s_%d%s", baseName, System.currentTimeMillis(), extension));
                log.warn("Target file {} already exists. Moving to {}", processedDirPath.resolve(originalFilename), targetFile);
            }

            Files.move(sourceFile, targetFile, StandardCopyOption.ATOMIC_MOVE);
            log.info("Moved processed file {} to {}", sourceFile, targetFile);
        } catch (IOException e) {
            log.error("Failed to move processed file {}: {}", sourceFile, e.getMessage(), e);
            // This error during move should not fail the main transaction of data ingestion
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started. Initiating CSV data ingestion...");
        try {
            ingestAllPendingCsvFiles();
            log.info("Initial CSV data ingestion complete.");
        } catch (Exception e) {
            log.error("A critical error occurred during initial CSV data ingestion process. Application startup sequence might be affected.", e);
        }
    }
}