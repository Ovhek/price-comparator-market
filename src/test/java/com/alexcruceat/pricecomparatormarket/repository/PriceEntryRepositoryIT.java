package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PriceEntryRepositoryIT extends AbstractIntegrationTest {

    @Autowired private PriceEntryRepository priceEntryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TestEntityManager entityManager;

    private Product testProduct;
    private Store testStore;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.saveAndFlush(new Brand("Test Brand PE"));
        Category category = categoryRepository.saveAndFlush(new Category("Test Cat PE"));
        testProduct = productRepository.saveAndFlush(new Product("Test Product PE", category, brand));
        testStore = storeRepository.saveAndFlush(new Store("Test Store PE"));
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Test
    @DisplayName("Save and retrieve PriceEntry")
    void whenSavePriceEntry_thenCanBeRetrieved() {
        PriceEntry entry = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(9.99), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        PriceEntry savedEntry = priceEntryRepository.saveAndFlush(entry);
        entityManager.clear();

        Optional<PriceEntry> foundOpt = priceEntryRepository.findById(savedEntry.getId());
        assertThat(foundOpt).isPresent();
        PriceEntry found = foundOpt.get();
        assertThat(found.getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(found.getStore().getId()).isEqualTo(testStore.getId());
        assertThat(found.getPrice()).isEqualByComparingTo("9.99");
        assertThat(found.getEntryDate()).isEqualTo(testDate);
    }

    @Test
    @DisplayName("Find PriceEntry by product, store, and date")
    void whenFindByProductStoreAndDate_thenReturnCorrectEntry() {
        PriceEntry entry = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(9.99), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        priceEntryRepository.saveAndFlush(entry);
        entityManager.clear();

        Optional<PriceEntry> foundOpt = priceEntryRepository.findByProductAndStoreAndEntryDate(testProduct, testStore, testDate);
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getPrice()).isEqualByComparingTo("9.99");
    }

    @Test
    @DisplayName("Saving duplicate PriceEntry (product, store, date) throws DataIntegrityViolationException")
    void whenSaveDuplicatePriceEntry_thenThrowException() {
        PriceEntry entry1 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(9.99), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        priceEntryRepository.saveAndFlush(entry1);

        PriceEntry entry2 = new PriceEntry(testProduct, testStore, "S002", BigDecimal.valueOf(8.88), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        // Should violate uk_price_entry_product_store_date
        assertThrows(DataIntegrityViolationException.class, () -> {
            priceEntryRepository.saveAndFlush(entry2);
        });
    }

    @Test
    @DisplayName("Find price history for product")
    void whenFindByProductOrderByEntryDateDesc_thenReturnHistory() {
        PriceEntry entry1 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(9.99), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        PriceEntry entry2 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(10.50), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate.plusDays(1));
        priceEntryRepository.saveAllAndFlush(List.of(entry1, entry2));
        entityManager.clear();

        List<PriceEntry> history = priceEntryRepository.findByProductOrderByEntryDateDesc(testProduct);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getEntryDate()).isEqualTo(testDate.plusDays(1)); // Most recent first
        assertThat(history.get(1).getEntryDate()).isEqualTo(testDate);
    }
    @Test
    @DisplayName("Find product price history in range")
    void whenFindProductPriceHistoryInRange_thenReturnCorrectEntries() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate midDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 30);
        LocalDate outsideDate = LocalDate.of(2024, 2, 1);

        PriceEntry entry1 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(10.00), "RON", BigDecimal.ONE, UnitOfMeasure.KG, startDate);
        PriceEntry entry2 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(11.00), "RON", BigDecimal.ONE, UnitOfMeasure.KG, midDate);
        PriceEntry entry3 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(12.00), "RON", BigDecimal.ONE, UnitOfMeasure.KG, endDate);
        PriceEntry entry4 = new PriceEntry(testProduct, testStore, "S001", BigDecimal.valueOf(13.00), "RON", BigDecimal.ONE, UnitOfMeasure.KG, outsideDate);
        priceEntryRepository.saveAllAndFlush(List.of(entry1, entry2, entry3, entry4));
        entityManager.clear();

        List<PriceEntry> history = priceEntryRepository.findByProductIdAndEntryDateBetweenOrderByEntryDateAsc(testProduct.getId(), startDate, endDate);
        assertThat(history).hasSize(3);
        assertThat(history).extracting(PriceEntry::getEntryDate).containsExactlyInAnyOrder(startDate, midDate, endDate);
        assertThat(history.get(0).getEntryDate()).isEqualTo(startDate); // Ordered by ASC
        assertThat(history.get(2).getEntryDate()).isEqualTo(endDate);
    }
}