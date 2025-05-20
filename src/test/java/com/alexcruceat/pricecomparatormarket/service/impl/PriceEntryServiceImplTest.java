package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import com.alexcruceat.pricecomparatormarket.repository.PriceEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PriceEntryServiceImpl}.
 * Mocks the {@link PriceEntryRepository} to test service logic for price entries.
 */
@ExtendWith(MockitoExtension.class)
class PriceEntryServiceImplTest {

    @Mock
    private PriceEntryRepository priceEntryRepositoryMock;

    @InjectMocks
    private PriceEntryServiceImpl priceEntryService;

    private Product testProduct;
    private Store testStore;
    private LocalDate testDate;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("Test Store");

        testDate = LocalDate.of(2024, 1, 20);
    }

    /**
     * Tests creation of a new price entry when no existing entry is found.
     */
    @Test
    @DisplayName("saveOrUpdatePriceEntry: creates new entry when none exists")
    void saveOrUpdatePriceEntry_createsNew_whenNoneExists() {
        // Given
        String storeProductId = "S001";
        BigDecimal price = new BigDecimal("9.99");
        String currency = "RON";
        BigDecimal packageQuantity = BigDecimal.ONE;
        UnitOfMeasure unit = UnitOfMeasure.KG;

        when(priceEntryRepositoryMock.findByProductAndStoreAndEntryDate(testProduct, testStore, testDate)).thenReturn(Optional.empty());
        // Mock the save operation to return the argument passed to it, potentially with an ID set
        when(priceEntryRepositoryMock.save(any(PriceEntry.class))).thenAnswer(invocation -> {
            PriceEntry entry = invocation.getArgument(0);
            entry.setId(100L); // Simulate ID generation
            return entry;
        });

        // When
        PriceEntry result = priceEntryService.saveOrUpdatePriceEntry(
                testProduct, testStore, testDate, storeProductId, price, currency, packageQuantity, unit
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getProduct()).isEqualTo(testProduct);
        assertThat(result.getStore()).isEqualTo(testStore);
        assertThat(result.getEntryDate()).isEqualTo(testDate);
        assertThat(result.getStoreProductId()).isEqualTo(storeProductId);
        assertThat(result.getPrice()).isEqualByComparingTo(price);
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getPackageQuantity()).isEqualByComparingTo(packageQuantity);
        assertThat(result.getPackageUnit()).isEqualTo(unit);

        verify(priceEntryRepositoryMock).findByProductAndStoreAndEntryDate(testProduct, testStore, testDate);
        verify(priceEntryRepositoryMock).save(any(PriceEntry.class));
    }

    /**
     * Tests updating an existing price entry.
     * Verifies that the existing entry's fields are updated and saved.
     */
    @Test
    @DisplayName("saveOrUpdatePriceEntry: updates existing entry with new details")
    void saveOrUpdatePriceEntry_updatesExisting_withNewDetails() {
        // Given
        PriceEntry existingEntry = new PriceEntry(testProduct, testStore, "S001", new BigDecimal("8.00"), "RON", BigDecimal.ONE, UnitOfMeasure.KG, testDate);
        existingEntry.setId(20L); // Simulate existing persisted entry

        String newStoreProductId = "S001-MOD";
        BigDecimal newPrice = new BigDecimal("7.50");
        String newCurrency = "EUR";
        BigDecimal newPackageQuantity = new BigDecimal("0.5");
        UnitOfMeasure newUnit = UnitOfMeasure.L;

        when(priceEntryRepositoryMock.findByProductAndStoreAndEntryDate(testProduct, testStore, testDate)).thenReturn(Optional.of(existingEntry));
        when(priceEntryRepositoryMock.save(any(PriceEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PriceEntry result = priceEntryService.saveOrUpdatePriceEntry(
                testProduct, testStore, testDate, newStoreProductId, newPrice, newCurrency, newPackageQuantity, newUnit
        );

        // Then
        ArgumentCaptor<PriceEntry> entryCaptor = ArgumentCaptor.forClass(PriceEntry.class);
        verify(priceEntryRepositoryMock).save(entryCaptor.capture());
        PriceEntry savedEntry = entryCaptor.getValue();

        assertThat(savedEntry).isNotNull();
        assertThat(savedEntry.getId()).isEqualTo(20L); // ID should remain the same
        assertThat(savedEntry.getStoreProductId()).isEqualTo(newStoreProductId);
        assertThat(savedEntry.getPrice()).isEqualByComparingTo(newPrice);
        assertThat(savedEntry.getCurrency()).isEqualTo(newCurrency);
        assertThat(savedEntry.getPackageQuantity()).isEqualByComparingTo(newPackageQuantity);
        assertThat(savedEntry.getPackageUnit()).isEqualTo(newUnit);
    }

    /**
     * Tests that findByProductAndStoreAndEntryDate delegates to the repository.
     */
    @Test
    @DisplayName("findByProductAndStoreAndEntryDate: delegates to repository")
    void findByProductAndStoreAndEntryDate_delegatesToRepository() {
        PriceEntry entry = new PriceEntry();
        when(priceEntryRepositoryMock.findByProductAndStoreAndEntryDate(testProduct, testStore, testDate))
                .thenReturn(Optional.of(entry));

        Optional<PriceEntry> result = priceEntryService.findByProductAndStoreAndEntryDate(testProduct, testStore, testDate);

        assertThat(result).isPresent().contains(entry);
        verify(priceEntryRepositoryMock).findByProductAndStoreAndEntryDate(testProduct, testStore, testDate);
    }

    /**
     * Tests that findByProductOrderByEntryDateDesc delegates to the repository.
     */
    @Test
    @DisplayName("findByProductOrderByEntryDateDesc: delegates to repository")
    void findByProductOrderByEntryDateDesc_delegatesToRepository() {
        List<PriceEntry> entries = Collections.singletonList(new PriceEntry());
        when(priceEntryRepositoryMock.findByProductOrderByEntryDateDesc(testProduct)).thenReturn(entries);

        List<PriceEntry> result = priceEntryService.findByProductOrderByEntryDateDesc(testProduct);

        assertThat(result).isEqualTo(entries);
        verify(priceEntryRepositoryMock).findByProductOrderByEntryDateDesc(testProduct);
    }

    /**
     * Tests that findProductPriceHistoryInRange delegates to the repository.
     */
    @Test
    @DisplayName("findProductPriceHistoryInRange: delegates to repository")
    void findProductPriceHistoryInRange_delegatesToRepository() {
        Long productId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<PriceEntry> entries = Collections.singletonList(new PriceEntry());
        when(priceEntryRepositoryMock.findProductPriceHistoryInRange(productId, startDate, endDate)).thenReturn(entries);

        List<PriceEntry> result = priceEntryService.findProductPriceHistoryInRange(productId, startDate, endDate);

        assertThat(result).isEqualTo(entries);
        verify(priceEntryRepositoryMock).findProductPriceHistoryInRange(productId, startDate, endDate);
    }

    /**
     * Tests that saveOrUpdatePriceEntry throws IllegalArgumentException for null product.
     */
    @Test
    @DisplayName("saveOrUpdatePriceEntry: throws IllegalArgumentException for null product")
    void saveOrUpdatePriceEntry_nullProduct_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> priceEntryService.saveOrUpdatePriceEntry(
                null, testStore, testDate, "S1", BigDecimal.ONE, "RON", BigDecimal.ONE, UnitOfMeasure.KG))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product cannot be null");
    }

}
