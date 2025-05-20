package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.config.AppProperties;
import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Integration tests for {@link com.alexcruceat.pricecomparatormarket.service.impl.CsvDataIngestionServiceImpl}.
 * Verifies the end-to-end CSV file ingestion process, including file reading,
 * data parsing, service logic orchestration, and database persistence.
 * Uses Testcontainers for a real MariaDB instance via {@link AbstractIntegrationTest}.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CsvDataIngestionServiceIT extends AbstractIntegrationTest {

    @Autowired
    private CsvDataIngestionService csvDataIngestionService;

    @MockitoSpyBean
    private AppProperties appPropertiesSpy;

    // Autowire repositories to verify data persisted to the database
    @Autowired private ProductRepository productRepository;
    @Autowired private PriceEntryRepository priceEntryRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private DiscountRepository discountRepository;

    @Autowired // Autowire TransactionTemplate
    private TransactionTemplate transactionTemplate;

    private Path inputTestDir;
    private Path processedTestDir;

    /**
     * Sets up temporary directories for CSV input and processed files before each test.
     * Configures the {@link AppProperties} spy to use these temporary directories.
     * Cleans the database to ensure test isolation.
     *
     * @param tempDirGlobal A temporary directory managed by JUnit for the test class.
     * @throws IOException If an error occurs creating directories.
     */
    @BeforeEach
    void setUp(@TempDir Path tempDirGlobal) throws IOException {
        inputTestDir = tempDirGlobal.resolve("test-csv-input");
        processedTestDir = tempDirGlobal.resolve("test-csv-processed");
        Files.createDirectories(inputTestDir);
        Files.createDirectories(processedTestDir);

        AppProperties.Csv csvProps = new AppProperties.Csv();
        csvProps.setInputPath(inputTestDir.toString());
        csvProps.setProcessedPath(processedTestDir.toString());
        doReturn(csvProps).when(appPropertiesSpy).getCsv();

        // Clean database tables before each test for isolation
        // Order is important due to foreign key constraints
        discountRepository.deleteAllInBatch();
        priceEntryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
    }

    /**
     * Tests the end-to-end ingestion of a valid product price CSV file.
     * Verifies that entities (Store, Brand, Category, Product, PriceEntry) are created
     * correctly in the database and the source CSV file is moved.
     *
     * @throws IOException If an error occurs during file operations.
     */
    @Test
    @DisplayName("ingestAllPendingCsvFiles: processes valid product price CSV, persists data, and moves file")
    void ingestAllPending_productPriceCsv_persistsDataAndMovesFile() throws IOException {
        // Given: Create a sample product price CSV in the temporary input directory
        Path productCsvFile = inputTestDir.resolve("lidl_2024-01-20.csv");
        String csvContent = "product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency\n" +
                "L001;Test Kefir;Lactate;Pilos;0.5;KG;4.99;RON\n" +
                "L002;Test Apa Minerala;Bauturi;Aqua;2;L;1,99;RON"; // Comma decimal
        Files.writeString(productCsvFile, csvContent);

        // When: Trigger the ingestion of all pending CSV files
        csvDataIngestionService.ingestAllPendingCsvFiles();

        // Then: Verify database state
        Store store = storeRepository.findByNameIgnoreCase("lidl").orElseThrow();
        assertThat(store).as("Store 'lidl' should be present.")
                .isNotNull()
                .extracting(Store::getName)
                .isEqualTo("lidl");

        List<Product> products = transactionTemplate.execute(status -> {
            List<Product> prods = productRepository.findAll();
            for (Product p : prods) {
                Hibernate.initialize(p.getBrand());
                Hibernate.initialize(p.getCategory());
            }
            return prods;
        });
        assertThat(products).hasSize(2);

        Optional<Product> kefirProductOpt = products.stream()
                .filter(p -> "Test Kefir".equalsIgnoreCase(p.getName()) && "Pilos".equalsIgnoreCase(p.getBrand().getName()))
                .findFirst();

        assertThat(kefirProductOpt).isPresent();
        Product kefirProduct = kefirProductOpt.get();
        assertThat(kefirProduct.getCategory().getName()).isEqualToIgnoringCase("Lactate");

        List<PriceEntry> kefirPrices = transactionTemplate.execute(status -> {
            // Re-fetch product within this transaction to ensure it's managed
            Product p = productRepository.findById(kefirProduct.getId()).orElseThrow();
            List<PriceEntry> pes = priceEntryRepository.findByProductOrderByEntryDateDesc(p);
            for (PriceEntry pe : pes) {
                Hibernate.initialize(pe.getStore());
            }
            return pes;
        });

        assertThat(kefirPrices).hasSize(1);
        PriceEntry kefirPriceEntry = kefirPrices.get(0);
        assertThat(kefirPriceEntry.getStore().getName()).isEqualToIgnoringCase("lidl");
        assertThat(kefirPriceEntry.getPrice()).isEqualByComparingTo("4.99");
        assertThat(kefirPriceEntry.getEntryDate()).isEqualTo(LocalDate.of(2024,1,20));
        assertThat(kefirPriceEntry.getStoreProductId()).isEqualTo("L001");

        Optional<Product> apaProductOpt = products.stream()
                .filter(p -> "Test Apa Minerala".equalsIgnoreCase(p.getName()) && "Aqua".equalsIgnoreCase(p.getBrand().getName()))
                .findFirst();
        assertThat(apaProductOpt).isPresent();
        List<PriceEntry> apaPrices = transactionTemplate.execute(status -> {
            Product p = productRepository.findById(apaProductOpt.get().getId()).orElseThrow();
            List<PriceEntry> pes = priceEntryRepository.findByProductOrderByEntryDateDesc(p);
            for (PriceEntry pe : pes) {
                Hibernate.initialize(pe.getStore());
            }
            return pes;
        });
        assertThat(apaPrices.getFirst().getPrice()).isEqualByComparingTo("1.99");


        // Verify file movement
        assertThat(Files.notExists(productCsvFile)).as("Source CSV file should be moved").isTrue();
        assertThat(Files.list(processedTestDir).count()).as("One file should be in processed directory").isEqualTo(1);
        Path movedFile = Files.list(processedTestDir).findFirst().get();
        assertThat(movedFile.getFileName().toString()).as("Moved file name check").startsWith("lidl_2024-01-20");
    }

    /**
     * Tests the end-to-end ingestion of a valid discount CSV file after its corresponding products exist.
     * Verifies that Discount entities are created correctly and linked to existing products and stores.
     *
     * @throws IOException If an error occurs during file operations.
     */
    @Test
    @DisplayName("ingestAllPendingCsvFiles: processes valid discount CSV after product CSV, persists data, and moves file")
    void ingestAllPending_discountCsv_persistsDataAndMovesFile() throws IOException {
        // Given: First, ensure products exist by ingesting a product file
        Path productCsvFile = inputTestDir.resolve("profi_2024-02-10.csv");
        String productCsvContent = "product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency\n" +
                "P789;Discounted Water;Bauturi;Izvorul Minunilor;1.5;L;2.50;RON";
        Files.writeString(productCsvFile, productCsvContent);
        csvDataIngestionService.ingestAllPendingCsvFiles(); // Ingest product first (moves productCsvFile)

        // Now, prepare and ingest the discount CSV for the same product
        Path discountCsvFile = inputTestDir.resolve("profi_discounts_2024-02-10.csv");
        String discountCsvContent = "product_id;product_name;brand;package_quantity;package_unit;product_category;from_date;to_date;percentage_of_discount\n" +
                "P789;Discounted Water;Izvorul Minunilor;1.5;L;Bauturi;2024-02-10;2024-02-20;20";
        Files.writeString(discountCsvFile, discountCsvContent);

        // When
        csvDataIngestionService.ingestAllPendingCsvFiles(); // Ingest discount

        // Then
        List<Discount> discounts = transactionTemplate.execute(status -> {
            List<Discount> ds = discountRepository.findAll();
            for (Discount d : ds) {
                Hibernate.initialize(d.getProduct());
                if (d.getProduct() != null) {
                    Hibernate.initialize(d.getProduct().getBrand());
                    Hibernate.initialize(d.getProduct().getCategory());
                }
                Hibernate.initialize(d.getStore());
            }
            return ds;
        });
        assertThat(discounts).hasSize(1);
        Discount persistedDiscount = discounts.get(0);

        Brand brand = brandRepository.findByNameIgnoreCase("Izvorul Minunilor").orElseThrow();
        Product discountedProduct = productRepository.findByNameIgnoreCaseAndBrand("Discounted Water", brand).orElseThrow();

        assertThat(persistedDiscount.getProduct().getId()).isEqualTo(discountedProduct.getId());
        assertThat(persistedDiscount.getStore().getName()).isEqualToIgnoringCase("profi");
        assertThat(persistedDiscount.getPercentage()).isEqualTo(20);
        assertThat(persistedDiscount.getFromDate()).isEqualTo(LocalDate.of(2024,2,10));
        assertThat(persistedDiscount.getToDate()).isEqualTo(LocalDate.of(2024,2,20));
        assertThat(persistedDiscount.getPackageQuantity()).isEqualByComparingTo("1.5");
        assertThat(persistedDiscount.getPackageUnit()).isEqualTo(UnitOfMeasure.L);
        assertThat(persistedDiscount.getRecordedAtDate()).isEqualTo(LocalDate.of(2024,2,10));


        assertThat(Files.notExists(discountCsvFile)).as("Discount CSV file should be moved").isTrue();
        assertThat(Files.list(processedTestDir).count()).as("Two files should be in processed directory").isEqualTo(2);
    }

    /**
     * Tests that processing a product CSV file with a row containing a critical data error
     * (which causes an exception in a handler service, leading to transaction rollback for that file)
     * results in no data from that file being persisted, and the file is not moved.
     *
     * @throws IOException If an error occurs during file operations.
     */
    @Test
    @DisplayName("ingestAllPendingCsvFiles: faulty product CSV processing rolls back")
    void ingestAllPending_faultyProductCsv_rollsBack() throws IOException {
        // Given
        Path faultyProductCsv = inputTestDir.resolve("faulty_kaufland_2024-03-01.csv");
        String csvContent = "product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency\n" +
                "K001;Good Product;Lactate;Pilos;1;L;5.00;RON\n" +
                "K002;;Lactate;Pilos;1;L;6.00;RON\n" + // Blank product name -> IAE from ProductService
                "K003;Another Good;Lactate;Pilos;2;L;10.00;RON";
        Files.writeString(faultyProductCsv, csvContent);

        // When
        csvDataIngestionService.ingestAllPendingCsvFiles();

        // Then

        List<Product> products = productRepository.findAll();
        assertThat(products).as("No products should be persisted from faulty file due to rollback").isEmpty();

        Optional<Store> storeOpt = storeRepository.findByNameIgnoreCase("kaufland");
        assertThat(storeOpt).as("Store 'kaufland' should also be rolled back").isNotPresent();
    }

    /**
     * Tests processing of two files: one valid, one invalid.
     * Verifies that data from the valid file is persisted and the file is moved,
     * while data from the invalid file is rolled back and the file is not moved.
     *
     * @throws IOException If an error occurs during file operations.
     */
    @Test
    @DisplayName("ingestAllPendingCsvFiles: processes one valid and one invalid file correctly")
    void ingestAllPending_oneValidOneInvalid_handlesCorrectly() throws IOException {
        // Given: Valid file
        Path validFile = inputTestDir.resolve("carrefour_2024-04-01.csv");
        String validContent = "product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency\n" +
                "C001;Apa Plata Izvorul;Bauturi;Izvorul;2;L;2.00;RON";
        Files.writeString(validFile, validContent);

        // Given: Invalid file (will cause parsing error in CsvFileReaderServiceImpl, skipping rows)
        // Let's make an error that CsvFileReaderService catches and skips, so file processing "succeeds"
        Path contentErrorFile = inputTestDir.resolve("auchan_2024-04-02.csv");
        String contentErrorFileContent = "product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency\n" +
                "A001;Ulei Floarea;Alimente Baza;Bunica;1;L;9.00;RON\n" +
                "A002;;Alimente Baza;Margaritar;1;KG;6.00;RON\n"+ // Blank product name
                "A003;Orez Bob Lung;Alimente Baza;Deroni;1;KG;5.00;RON";
        Files.writeString(contentErrorFile, contentErrorFileContent );


        // When
        csvDataIngestionService.ingestAllPendingCsvFiles();

        // Then: Check data from valid file
        Optional<Store> carrefourStore = storeRepository.findByNameIgnoreCase("carrefour");
        assertThat(carrefourStore).isPresent();

        Product apaIzvorul = transactionTemplate.execute(status -> {
            Brand izvorulBrand = brandRepository.findByNameIgnoreCase("Izvorul").orElseThrow();
            Product p = productRepository.findByNameIgnoreCaseAndBrand("Apa Plata Izvorul", izvorulBrand).orElseThrow();
            Hibernate.initialize(p.getBrand()); // already used for query
            Hibernate.initialize(p.getCategory());
            return p;
        });

        assert apaIzvorul != null;
        assertThat(apaIzvorul.getCategory().getName()).isEqualToIgnoringCase("Bauturi");

        List<PriceEntry> apaIzvorulPrices = priceEntryRepository.findByProductOrderByEntryDateDesc(apaIzvorul);
        assertThat(apaIzvorulPrices).hasSize(1);

        // Then: Check data from file with content error (auchan_2024-04-02.csv)
        // The transaction for this file should have rolled back.
        Optional<Store> auchanStore = storeRepository.findByNameIgnoreCase("auchan");
        assertThat(auchanStore).as("Store 'auchan' from rolled-back file should not exist").isNotPresent();

        Optional<Brand> bunicaBrand = brandRepository.findByNameIgnoreCase("Bunica");
        assertThat(bunicaBrand).as("Brand 'Bunica' from rolled-back file should not exist").isNotPresent();
        Optional<Brand> deroniBrand = brandRepository.findByNameIgnoreCase("Deroni");
        assertThat(deroniBrand).as("Brand 'Deroni' from rolled-back file should not exist").isNotPresent();
        Optional<Brand> margaritarBrand = brandRepository.findByNameIgnoreCase("Margaritar");
        assertThat(margaritarBrand).as("Brand 'Margaritar' from rolled-back file should not exist").isNotPresent();

        // Verify file movements
        assertThat(Files.notExists(validFile)).as("Valid file should be moved").isTrue();
        assertThat(Files.exists(contentErrorFile)).as("File with content error causing rollback should NOT be moved").isTrue();
        assertThat(Files.list(processedTestDir).count()).as("Only the valid file should be in processed directory").isEqualTo(1);
    }
}
