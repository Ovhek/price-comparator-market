package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Discount;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import com.alexcruceat.pricecomparatormarket.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * Unit tests for {@link DiscountServiceImpl}.
 * Mocks {@link DiscountRepository} to test service logic for discounts.
 */
@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private DiscountRepository discountRepositoryMock;

    @InjectMocks
    private DiscountServiceImpl discountService;

    private Product testProduct;
    private Store testStore;
    private LocalDate testFromDate;
    private LocalDate baseToDate;
    private LocalDate baseRecordedAtDate;
    private BigDecimal testPackageQuantity;
    private UnitOfMeasure testPackageUnit;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product(); testProduct.setId(1L);
        testStore = new Store(); testStore.setId(1L);
        testFromDate = LocalDate.of(2024, 1, 10);
        baseToDate = LocalDate.of(2024, 1, 20);
        baseRecordedAtDate = LocalDate.of(2024, 1, 15); // Existing is recorded on the 15th
        testPackageQuantity = BigDecimal.ONE;
        testPackageUnit = UnitOfMeasure.BUCATA;
    }

    /**
     * Tests creation of a new discount when no existing one is found.
     */
    @Test
    @DisplayName("saveOrUpdateDiscount: creates new discount when none exists")
    void saveOrUpdateDiscount_createsNew_whenNoneExists() {
        // Given
        Integer percentage = 15;
        when(discountRepositoryMock.findByProductAndStoreAndFromDateAndPackageQuantityAndPackageUnit(
                testProduct, testStore, testFromDate, testPackageQuantity, testPackageUnit))
                .thenReturn(Optional.empty());
        when(discountRepositoryMock.save(any(Discount.class))).thenAnswer(invocation -> {
            Discount d = invocation.getArgument(0);
            d.setId(10L); // Simulate ID generation
            return d;
        });

        // When
        Discount result = discountService.saveOrUpdateDiscount(
                testProduct, testStore, testPackageQuantity, testPackageUnit,
                percentage, testFromDate, baseToDate, baseRecordedAtDate // Using base dates
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getPercentage()).isEqualTo(percentage);
        verify(discountRepositoryMock).save(any(Discount.class));
    }

    /**
     * Tests updating an existing discount when new data is different and newer.
     */
    @Test
    @DisplayName("saveOrUpdateDiscount: updates existing discount when data differs and is newer")
    void saveOrUpdateDiscount_updatesExisting_whenDataDiffersAndIsNewer() {
        // Given
        Discount existingDiscount = new Discount(
                testProduct,
                testStore,
                10,
                testFromDate,
                baseToDate.minusDays(1),
                baseRecordedAtDate.minusDays(1),
                testPackageQuantity,
                testPackageUnit
        );
        existingDiscount.setId(5L);

        Integer newPercentage = 20;
        LocalDate newToDate = baseToDate;
        LocalDate newRecordedAtDate = baseRecordedAtDate;

        when(discountRepositoryMock.findByProductAndStoreAndFromDateAndPackageQuantityAndPackageUnit(
                testProduct, testStore, testFromDate, testPackageQuantity, testPackageUnit))
                .thenReturn(Optional.of(existingDiscount));
        when(discountRepositoryMock.save(any(Discount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Discount result = discountService.saveOrUpdateDiscount(
                testProduct, testStore, testPackageQuantity, testPackageUnit,
                newPercentage, testFromDate, newToDate, newRecordedAtDate
        );

        // Then
        ArgumentCaptor<Discount> discountCaptor = ArgumentCaptor.forClass(Discount.class);
        verify(discountRepositoryMock).save(discountCaptor.capture());
        Discount savedDiscount = discountCaptor.getValue();

        assertThat(savedDiscount.getId()).isEqualTo(5L);
        assertThat(savedDiscount.getPercentage()).isEqualTo(newPercentage);
        assertThat(savedDiscount.getToDate()).isEqualTo(newToDate);
        assertThat(savedDiscount.getRecordedAtDate()).isEqualTo(newRecordedAtDate);
    }

    /**
     * Tests that an existing discount is NOT updated if the new data is the same or older.
     * The service should return the existing, unchanged discount.
     */
    @Test
    @DisplayName("saveOrUpdateDiscount: does not update existing discount when data is same or older, returns existing")
    void saveOrUpdateDiscount_noUpdate_whenDataSameOrOlder() {
        // Given
        Integer existingPercentage = 10;
        LocalDate existingToDate = baseToDate;
        LocalDate existingRecordedAtDate = baseRecordedAtDate;

        Discount existingDiscount = new Discount(
                testProduct,
                testStore,
                existingPercentage,
                testFromDate,
                existingToDate,
                existingRecordedAtDate,
                testPackageQuantity,
                testPackageUnit
        );
        existingDiscount.setId(5L);

        when(discountRepositoryMock.findByProductAndStoreAndFromDateAndPackageQuantityAndPackageUnit(
                testProduct, testStore, testFromDate, testPackageQuantity, testPackageUnit))
                .thenReturn(Optional.of(existingDiscount));

        // Scenario 1: Input data is exactly the same
        Discount resultSameData = discountService.saveOrUpdateDiscount(
                testProduct, testStore, testPackageQuantity, testPackageUnit,
                existingPercentage, testFromDate, existingToDate, existingRecordedAtDate // Same data
        );
        assertThat(resultSameData).isNotNull(); // Expecting the service to return the existing object
        assertThat(resultSameData.getId()).isEqualTo(existingDiscount.getId());
        assertThat(resultSameData.getPercentage()).isEqualTo(existingPercentage);
        // No save should be called for same data
        verify(discountRepositoryMock, never()).save(existingDiscount);


        // Scenario 2: Input data has an older recordedAtDate (and other fields same or older)
        LocalDate olderRecordedAtDate = existingRecordedAtDate.minusDays(1); // Input recorded on the 14th
        LocalDate olderToDate = existingToDate.minusDays(1);

        Discount resultOlderData = discountService.saveOrUpdateDiscount(
                testProduct, testStore, testPackageQuantity, testPackageUnit,
                existingPercentage, // Same percentage
                testFromDate,      // Same fromDate
                olderToDate,       // Older toDate
                olderRecordedAtDate // Older recordedAtDate
        );

        assertThat(resultOlderData).isNotNull();
        assertThat(resultOlderData.getId()).isEqualTo(existingDiscount.getId());

        verify(discountRepositoryMock, never()).save(any(Discount.class));
    }

    /**
     * Tests that findActiveDiscountsByDate delegates to the repository.
     */
    @Test
    @DisplayName("findActiveDiscountsByDate: delegates to repository")
    void findActiveDiscountsByDate_delegatesToRepository() {
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Discount> expectedPage = new PageImpl<>(Collections.singletonList(new Discount()));
        when(discountRepositoryMock.findActiveDiscountsByDate(date, pageable)).thenReturn(expectedPage);

        Page<Discount> result = discountService.findActiveDiscountsByDate(date, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(discountRepositoryMock).findActiveDiscountsByDate(date, pageable);
    }
}
